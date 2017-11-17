'use strict';

var constants = require('./constants');
var electron = require('electron');
var http = require('http');

var app = electron.app;
var BrowserWindow = electron.BrowserWindow;
var mainWindow = null;
exports.loadIndex = loadIndex;

app.on('ready', function() {
    mainWindow = new BrowserWindow({
        height: 650,
        width: 750,
        resizable: false
    });

    mainWindow.on('close', function() {
      app.quit();
   });

    loadIndex();
});

function loadIndex()
{
  var options = { hostname: constants.API_URL, port: constants.API_PORT, path: '/', timeout: 8000, agent: false, method: 'GET' };
  var req = http.request(options, function(res) {
    mainWindow.loadURL('file://' + __dirname + '/app/index.html');
  });

  req.on('error', function(error) {
    mainWindow.loadURL('file://' + __dirname + '/app/index.html?error=' + constants.CODE_ERR_CONNECTION);
  });

  req.end();
}
