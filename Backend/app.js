var MongoClient = require('mongodb').MongoClient;
var ObjectID = require('mongodb').ObjectID;
var express = require('express');
var util = require('util');
var qs = require('querystring');
var fs = require('fs');
var url = require('url');
var pdf = require('html-pdf');
var app = express()

var db_url = 'mongodb://localhost:27017/bookdb';
var BOOK_COLLECTION = 'books';
var ADMIN_KEY = 'kpcofgs';
var PORT = 7999;
var RESULTS_PER_PAGE = 10;

var ERROR_PATH = '{"status": "fail", "code": 400, "message": "Not a valid API call."}';
var ERROR_DB = '{"status": "fail", "code": 400, "message": "Database error."}';
var ERROR_BAD_ID = '{"status": "fail", "code": 400, "message": "Invalid book ID."}';
var ERROR_ADD = '{"status": "fail", "code": 400, "message": "Error adding book."}';
var ERROR_CLEAR = '{"status": "fail", "code": 400, "message": "Error clearing database."}';
var ERROR_AUTH = '{"status": "fail", "code": 400, "message": "Authentication error."}';
var ERROR_EDIT = '{"status": "fail", "code": 400, "message": "Error saving book."}';
var ERROR_PDF = '{"status": "fail", "code": 400, "message": "Error generating pdf."}';

var SUCCESS_DELETE = '{"status": "success", "code": 200, "message": "Book deleted."}';
var SUCCESS_READY = '{"status": "success", "code": 200, "message": "Ready."}';
var SUCCESS_ADD = '{"status": "success", "code": 200, "message": "Book added."}';
var SUCCESS_CLEAR = '{"status": "success", "code": 200, "message": "Database cleared."}';
var SUCCESS_EDIT = '{"status": "success", "code": 200, "message": "Book saved."}';

app.get('/app', function(req, res)
{
	var file = __dirname + '/Librarian.zip';
	res.download(file);
});

app.get('/count', function(req, res)
{
	MongoClient.connect(db_url, function(err, db) {
		var collection = db.collection(BOOK_COLLECTION).count({}, function(err, num)
		{
			if(err)
			{
				res.writeHead(400, {'Content-Type': 'application/json'});
				res.end(ERROR_DB);
			}
			else
			{
				res.writeHead(200, {'Content-Type': 'application/json'});
				res.end('{"status": "success", "code": 200, "count": ' + num + '}');
			}
			db.close();
		});
	});
});

app.get('/pdf', function(req, res)
{
	MongoClient.connect(db_url, function(err, db) {
		var collection = db.collection(BOOK_COLLECTION);
		collection.find({}).toArray(function(err, docs)
		{
			if(err)
			{
				console.log(err);
				res.writeHead(400, {'Content-Type': 'application/json'});
				res.end(ERROR_DB);
			}
			else
			{
				var html = "<html><head><link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\"></head><body>";
				for(var i in docs)
				{
					var book = "";
					var isbn = docs[i].isbn;
					var title = docs[i].title;
					var edition = docs[i].edition;
					var authors = docs[i].authors;
					var publication = docs[i].publication;
					var img_url = docs[i].img_url;
					var tags = docs[i].tags;
					var notes = docs[i].notes;

					book += '<a href="#" class="list-group-item list-group-item-action flex-column align-items-start">';
					book += '<table><tr><td>';
					if(img_url)
					{
						book += '<img width="70" height="90" src="' + img_url + '"/>&nbsp;&nbsp;';
					}
					else
					{
						book += '<img width="70" height="90" src="assets/images/book_placeholder.png"/>&nbsp;&nbsp;';
					}
					book += '</td><td>';
					book += '<h5 class="mb-1"><a href="#">' + title + '</a></h5>';
					if(edition)
					{
						book += '<p class="mb-2">' + edition + '</p>';
					}
					book += '<p class="mb-1">';
					if(authors)
					{
						book += authors;
					}
					if(publication)
					{
						if(authors)
						{
							book += " \u2022 ";
						}
						book += '<i>' + publication + '</i>';
					}
					book += '</p>';
					if(tags)
					{
						book += '<small>Tags: <font color="#E91E63">' + tags + '</font></small><br/>';
					}
					if(notes)
					{
						book += '<small>Notes: <font color="#4CAF50">' + notes + '</font></small>';
					}
					book += '</td></tr></table></a>';
					html += book;
				}
				html += "</body></html>";

				pdf.create(html).toFile('./librarian.pdf', function(err, doc)
				{
					if(err)
					{
						console.log(err);
						res.writeHead(400, {'Content-Type': 'application/json'});
						res.end(ERROR_PDF);
					}
					else
					{
						res.download('./librarian.pdf');
					}
				});
			}
		});
	});
});

app.get('/listall', function(req, res)
{
	MongoClient.connect(db_url, function(err, db) {
		var collection = db.collection(BOOK_COLLECTION);
		collection.find({}).toArray(function(err, docs)
		{
			if(err)
			{
				console.log(err);
				res.writeHead(400, {'Content-Type': 'application/json'});
				res.end(ERROR_DB);
			}
			else
			{
				res.jsonp(docs);
			}
			db.close();
		});
	});
});

app.get('/list', function(req, res)
{
	var args = url.parse(req.url, true);
	var page = 0;
	var results_per_page = RESULTS_PER_PAGE;

	if(args.query.page)
	{
		page = parseInt(args.query.page);
	}

	if(args.query.rpp)
	{
		results_per_page = parseInt(args.query.rpp);
	}

	MongoClient.connect(db_url, function(err, db) {
		var collection = db.collection(BOOK_COLLECTION);
		collection.find({}).skip(page * results_per_page).limit(results_per_page).toArray(function(err, docs)
		{
			if(err)
			{
				res.writeHead(400, {'Content-Type': 'application/json'});
				res.end(ERROR_DB);
			}
			else
			{
				res.jsonp(docs);
			}
			db.close();
		});
	});
});

app.get('/search/:query', function(req, res)
{
	var query = req.params.query;
	MongoClient.connect(db_url, function(err, db) {
		var collection = db.collection(BOOK_COLLECTION);
		collection.find({ $or: [{title: new RegExp(query, "i")}, {authors: new RegExp(query, "i")}, {tags: new RegExp(query, "i")}, {isbn: new RegExp(query, "i")}]}).toArray(function(err, docs)
		{
			if(err)
			{
				res.writeHead(400, {'Content-Type': 'application/json'});
				res.end(ERROR_DB);
			}
			else
			{
				res.jsonp(docs);
			}
			db.close();
		});
	});
});

app.get('/get/:bookID', function(req, res)
{
	try
	{
		var bookID = req.params.bookID;
		var oid = new ObjectID(bookID);
		MongoClient.connect(db_url, function(err, db) {
			var collection = db.collection(BOOK_COLLECTION);
			collection.findOne({"_id": oid}, function(err, doc)
			{
				if(err)
				{		
					res.writeHead(400, {'Content-Type': 'application/json'});
					res.end(ERROR_DB);
				}
				else
				{
					res.jsonp(doc);
				}
				db.close();
			});
		});
	}
	catch(ex)
	{
		res.writeHead(400, {'Content-Type': 'application/json'});
		res.end(ERROR_BAD_ID);
		db.close();
	}
});

app.post('/add', function(req, res)
{
	if(req.method == 'POST')
	{
		var body = '';
		req.on('data', function(data)
		{
			body += data;
			if(body.length > 1e6)
			{
				req.connection.destroy();
			}
		});

		req.on('end', function()
		{
			var obj = JSON.parse(body);
			
			MongoClient.connect(db_url, function(err, db) {
				var collection = db.collection(BOOK_COLLECTION);
				collection.insertOne(obj, function(err, r)
				{
					if(err)
					{
						console.log(err);
						res.writeHead(400, {'Content-Type': 'application/json' });
						res.end(ERROR_ADD);
						db.close();
						return;
					}
					db.close();
				});
			});

			res.writeHead(200, {'Content-Type': 'application/json' });
			res.end(SUCCESS_ADD);
		});
	}
	else
	{
		res.writeHead(400, {'Content-Type': 'application/json' });
		res.end(ERROR_PATH);
	}
});

app.post('/edit/:bookID', function(req, res)
{
	if(req.method == 'POST')
	{
		var body = '';
		req.on('data', function(data)
		{
			body += data;
			if(body.length > 1e6)
			{
				req.connection.destroy();
			}
		});

		req.on('end', function()
		{
			var obj = JSON.parse(body);
			var bookID = req.params.bookID;
			var oid = new ObjectID(bookID);
			
			MongoClient.connect(db_url, function(err, db) {
				var collection = db.collection(BOOK_COLLECTION);
				collection.update({"_id": oid}, obj, function(err, r)
				{
					if(err)
					{
						console.log(err);
						res.writeHead(400, {'Content-Type': 'application/json' });
						res.end(ERROR_EDIT);
						return;
					}
					db.close();
				});
			});

			res.writeHead(200, {'Content-Type': 'application/json' });
			res.end(SUCCESS_EDIT);
		});
	}
	else
	{
		res.writeHead(400, {'Content-Type': 'application/json' });
		res.end(ERROR_PATH);
	}
});

app.get('/clear', function(req, res)
{
	var args = url.parse(req.url, true);
	var key = '';

	if(args.query.key)
	{
		key = args.query.key;
	}

	if(key != ADMIN_KEY)
	{
		res.writeHead(400, {'Content-Type': 'application/json' });
		res.end(ERROR_AUTH);
		return;
	}

	MongoClient.connect(db_url, function(err, db) {
		var collection = db.collection(BOOK_COLLECTION);
		collection.remove({}, function(err)
		{
			if(err)
			{
				res.writeHead(400, {'Content-Type': 'application/json' });
				res.end(ERROR_CLEAR);
				return;
			}
			db.close();
		});
	});
	
	res.writeHead(200, {'Content-Type': 'application/json' });
	res.end(SUCCESS_CLEAR);
});

app.get('/delete/:bookID', function(req, res)
{
	try
	{
		var bookID = req.params.bookID;
		var oid = new ObjectID(bookID);
		MongoClient.connect(db_url, function(err, db) {
			var collection = db.collection(BOOK_COLLECTION);
			collection.deleteOne({"_id": oid}, function(err, doc)
			{
				if(err)
				{		
					res.writeHead(400, {'Content-Type': 'application/json'});
					res.end(ERROR_DB);
				}
				else
				{
					res.writeHead(200, {'Content-Type': 'application/json'});
					res.end(SUCCESS_DELETE);
				}
				db.close();
			});
		});
	}
	catch(ex)
	{
		res.writeHead(400, {'Content-Type': 'application/json'});
		res.end(ERROR_BAD_ID);
		db.close();
	}
});

app.get('/', function(req, res)
{
	res.writeHead(200, {'Content-Type': 'application/json'});
	res.end(SUCCESS_READY);
	return;
});

app.get('*', function(req, res)
{
	res.writeHead(400, {'Content-Type': 'application/json'});
	res.end(ERROR_PATH);
	return;
});

var server = app.listen(PORT, function()
{
	var host = server.address().address
	var port = server.address().port
});
