var cordova = require('cordova');

function LocationPlugin() {
}

LocationPlugin.prototype.watchLocation = function ({ timeout, success, error }) {
  cordova.exec(success, error, "LocationPlugin", "watchlocation", [timeout]);
};

LocationPlugin.prototype.getLocation = function ({ success, error }) {
  cordova.exec(success, error, "LocationPlugin", "getlocation", []);
};

module.exports = new LocationPlugin();
