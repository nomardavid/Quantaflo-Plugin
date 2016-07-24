window.setConnectionCallback = function(callback)
{
    cordova.exec(callback,
                 function(err){callback('setConnectionCallback falied');},
                 "QuantaFloSensorPlugin",
                 "setConnectionCallback",
                 []);
};

window.getSensorProductId = function(callback)
{
    cordova.exec(callback,
                 function(err){callback('getSensorProductId falied');},
                 "QuantaFloSensorPlugin",
                 "getSensorProductId",
                 []);
};

window.getSensorSerialNumber = function(callback)
{
    cordova.exec(callback,
                 function(err){callback('getSensorSerialNumber falied');},
                 "QuantaFloSensorPlugin",
                 "getSensorSerialNumber",
                 []);
};

window.getSensorProductVersion = function(callback)
{
    cordova.exec(callback,
                 function(err){callback('getSensorProductVersion falied');},
                 "QuantaFloSensorPlugin",
                 "getSensorProductVersion",
                 []);
};

window.getSensorUses = function(callback)
{
    cordova.exec(callback,
                 function(err){callback('getSensorUses falied');},
                 "QuantaFloSensorPlugin",
                 "getSensorUses",
                 []);
};

window.getSensorPositives = function(callback)
{
    cordova.exec(callback,
                 function(err){callback('getSensorPositives falied');},
                 "QuantaFloSensorPlugin",
                 "getSensorPositives",
                 []);
};

window.getSensorGraceUsesRemaining = function(callback)
{
    cordova.exec(callback,
                 function(err){callback('getSensorGraceUsesRemaining falied');},
                 "QuantaFloSensorPlugin",
                 "getSensorGraceUsesRemaining",
                 []);
};

window.getSensorLastSyncTimeUtc = function(callback)
{
    cordova.exec(callback,
                 function(err){callback('getSensorLastSyncTimeUtc falied');},
                 "QuantaFloSensorPlugin",
                 "getSensorLastSyncTimeUtc",
                 []);
};

window.getSensorSyncFreqDays = function(callback)
{
    cordova.exec(callback,
                 function(err){callback('getSensorSyncFreqDays falied');},
                 "QuantaFloSensorPlugin",
                 "getSensorSyncFreqDays",
                 []);
};

window.getSensorCustomerId = function(callback)
{
    cordova.exec(callback,
                 function(err){callback('getSensorCustomerId falied');},
                 "QuantaFloSensorPlugin",
                 "getSensorCustomerId",
                 []);
};

window.getSensorPaymentStatus = function(callback)
{
    cordova.exec(callback,
                 function(err){callback('getSensorPaymentStatus falied');},
                 "QuantaFloSensorPlugin",
                 "getSensorPaymentStatus",
                 []);
};

//patientBirthday should be a string formatted as yyyy-MM-dd
window.setPatientData = function(patientLastName, patientId, patientBirthday, callback)
{
    cordova.exec(callback,
                 function(err){callback('setPatientData falied');},
                 "QuantaFloSensorPlugin",
                 "setPatientData",
                 [patientLastName, patientId, patientBirthday]);
};

window.setPulseDataReceivedCallback = function(callback)
{
    cordova.exec(callback,
                 function(err){callback('setPulseDataReceivedCallback falied');},
                 "QuantaFloSensorPlugin",
                 "setPulseDataReceivedCallback",
                 []);
};

var SideLimb =
{
    LEFT_FOOT : 0,
    LEFT_HAND : 1,
    RIGHT_FOOT : 2,
    RIGHT_HAND : 3
};

//values that end in FINAL mark the end of the measurement; values that do not end in final are intermediate responses (more will follow)
var StartMeasurementResponse =
{
    STARTED : 0,
    NOT_ADJUSTING : 1,
    ENDED_SUCCESSFULLY_FINAL : 2,
    CANCELED_FINAL : 3,
    AMBIENT_LIGHT_FINAL : 4,
    ERROR_FINAL : 5,
    NOT_CONNECTED_FINAL : 6,
    WRONG_STATE_FINAL : 7
}

window.startMeasurement = function(sideLimb, callback)
{
    cordova.exec(callback,
                 function(err){callback('startMeasurement falied');},
                 "QuantaFloSensorPlugin",
                 "startMeasurement",
                 [sideLimb]);
};

window.cancelMeasurement = function(callback)
{
    cordova.exec(callback,
                 function(err){callback('cancelMeasurement falied');},
                 "QuantaFloSensorPlugin",
                 "cancelMeasurement",
                 []);
};

window.newTest = function(callback)
{
    cordova.exec(callback,
                 function(err){callback('newTest falied');},
                 "QuantaFloSensorPlugin",
                 "newTest",
                 []);
};

window.getLeftResult = function(callback)
{
    cordova.exec(callback,
                 function(err){callback('getLeftResult falied');},
                 "QuantaFloSensorPlugin",
                 "getLeftResult",
                 []);
}

window.getRightResult = function(callback)
{
    cordova.exec(callback,
                 function(err){callback('getRightResult falied');},
                 "QuantaFloSensorPlugin",
                 "getRightResult",
                 []);
}

window.getLastSyncTime = function(callback)
{
    cordova.exec(callback,
                 function(err){callback('getLastSyncTime falied');},
                 "QuantaFloSensorPlugin",
                 "getLastSyncTime",
                 []);
};

//online sync now
window.syncNow = function(callback)
{
    cordova.exec(callback,
                 function(err){callback('syncNow falied');},
                 "QuantaFloSensorPlugin",
                 "syncNow",
                 []);
};

window.getSyncSensorCode = function(callback)
{
    cordova.exec(callback,
                 function(err){callback('getSyncSensorCode falied');},
                 "QuantaFloSensorPlugin",
                 "getSyncSensorCode",
                 []);
};

window.enterSyncUpdateCode = function(updateCode, callback)
{
    cordova.exec(callback,
                 function(err){callback('enterSyncUpdateCode falied');},
                 "QuantaFloSensorPlugin",
                 "enterSyncUpdateCode",
                 [updateCode]);
};

//////below this line will be deleted eventually//////

window.send = function(str, callback)
{
    cordova.exec(callback,
                 function(err){callback('send failed');},
                 "QuantaFloSensorPlugin",
                 "send",
                 [str]);
};

window.startReceiving = function(callback)
{
    cordova.exec(callback,
                 function(err){callback('startReceiving falied');},
                 "QuantaFloSensorPlugin",
                 "startReceiving",
                 []);
};
