var cordova = require('cordova');

function LocationPlugin() {
}

LocationPlugin.prototype.getLocation = function (enableBackgroundLocation, successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, "LocationPlugin", "getlocation", [enableBackgroundLocation]);
};

module.exports = new LocationPlugin();
