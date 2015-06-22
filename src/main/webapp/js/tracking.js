var interval = ""
var deviceData = new Array();
var googleMap;
var barcodePositioningMap;
var geoFencingPositioningMap;
var markersArray = [];
var barcodesArray = [];
var barcodesGPSArray = [];
var circlesArray = [];
var infowindow;
var projection;
var overlay;
var currentIndex = 0;
var employeeColors = [];
var employees = [];
var selectedEmployeeTracking = [];

function moveSelect(val) {
    var timeLineVar = jQuery("#slider").slider('value');
    if (hasAnotherValue(timeLineVar, val)) {
        changeMap(timeLineVar, currentIndex);
    } else {
        timeLineVar = timeLineVar + (val * 5);
        jQuery('#slider').slider('value', timeLineVar);

        var time = (timeLineVar / 1440) * 100;
        var hourReadable = (Math.floor(timeLineVar / 60) * 100);
        var minReadable = Math.floor(timeLineVar % 60);
        time = hourReadable + minReadable;
        jQuery("#gps").val(time);

        changeMap(timeLineVar, currentIndex);
    }
}

function setupBarcodeMap() {

}

function handlePointClick(event) {
    $("[id*=':lat']").val(event.latLng.lat());
    $("[id*=':lng']").val(event.latLng.lng());

    dlg.show();
}

function setSliderTicks() {
    var slider = jQuery('#slider');
    var max = slider.slider("option", "max");

    slider.find('.ui-slider-tick').remove();
    for (var i = 60; i < max; i += 60) {
        var time = (i / max) * 100;

        var hourReadable = ((i / 60) * 100).toString();

        $('<span class="ui-slider-tick"></span>').css('left', (time) + '%').appendTo(slider);
        $('<span class="ui-slider-label" style="color: black;">' + hourReadable + '</span>').css('left', (time) + '%').appendTo(slider);
    }
}

function setupSlider() {
    $(".ui-slider-filled-in").remove();

    var slider = jQuery('#slider');
    var max = slider.slider("option", "max");
    Object.keys(deviceData).forEach(function (key) {
        if (key == '2345') {
            var blah = key;
        }
        var minutes = key.substring(key.length - 2, key.length);
        var hour = key.substring(0, key.length - 2);
        if (hour == '') {
            hour = 0;
        }
        var time = (parseInt(hour) * 60) + parseInt(minutes);
        var indexSpacing = (time / max) * 100;
        $('<span class="ui-slider-filled-in"></span>').css('left', indexSpacing + '%').appendTo(slider);
    });

}

/**
 * This function is called when a barcode marker is dropped after a drag.
 */
function dragChanged(event) {
    var cid = document.getElementById('trackingForm:cid').value;
    var companyId = document.getElementById('trackingFormcompanyId').value;

    var eventUrl = 'updatebarcodegpsservlet?waypointid=' + this.markerId + '&lat=' + event.latLng.lat() + '&lng=' + event.latLng.lng() + "&companyId=" + companyId;
    $.ajax({
        type: 'GET',
        url: eventUrl,
        data: '{}',
        success: loadBarcodeCoordinatesOnMap
    });
}

function loadGeoFences() {
    var companyId = document.getElementById('trackingForm:companyId').value;
    var clientId = document.getElementById('trackingForm:clientId').value;
    $.ajax({
        type: 'GET',
        url: 'getfencesservlet?client_id=' + clientId + "&companyId=" + companyId,
        data: '{}',
        success: geofenceload
    });
}

function geofenceload(geoFenceData) {
    for (var i = 0; i < geoFenceData.length; i++) {
        var geoFence = geoFenceData[i];
        var points = geoFence.points;
        var polygonCoords = new Array();

        for (var p = 0; p < points.length; p++) {
            var geoPoint = points[p];
            var latitude = geoPoint.latitude;
            var longitude = geoPoint.longitude;

            polygonCoords.push(new google.maps.LatLng(latitude, longitude));
        }

        var geoFencePolygon = new google.maps.Polygon({
            paths: polygonCoords,
            strokeColor: '#FF0000',
            strokeOpacity: 0.8,
            strokeWeight: 2,
            fillColor: '#FF0000',
            fillOpacity: 0.35
        });

        geoFencePolygon.setMap(geoFencingPositioningMap.map);
    }
}

function setupGeoFencing() {
    var myGeoFenceLoadInterval = setInterval(function () {
        myTimer()
    }, 500);

    function myTimer() {
        geoFencingPositioningMap = PF('geoFencingPositioningMap');
        if (geoFencingPositioningMap != null) {
            if (geoFencingPositioningMap.getMap() != null) {
                clearInterval(myGeoFenceLoadInterval);
                var listenerHandle = google.maps.event.addListener(geoFencingPositioningMap.map, 'tilesloaded', function () {
                    var drawingManager = new google.maps.drawing.DrawingManager({
                        drawingMode: google.maps.drawing.OverlayType.MARKER,
                        drawingControl: true,
                        drawingControlOptions: {
                            position: google.maps.ControlPosition.TOP_CENTER,
                            drawingModes: [
                                google.maps.drawing.OverlayType.POLYLINE,
                            ]
                        }

                    });

                    drawingManager.setMap(geoFencingPositioningMap.map);
                    google.maps.event.removeListener(listenerHandle);

                    google.maps.event.addDomListener(drawingManager, 'polylinecomplete', function (polyline) {
                        var path = polyline.getPath();
                        var pathData = new Array();
                        for (var p = 0; p < path.length; p++) {
                            var currPos = path.getAt(p);
                            var lat = currPos.lat();
                            var lng = currPos.lng();
                            var currData = new Object();
                            currData.latitude = lat;
                            currData.longitude = lng;
                            currData.pointCount = p;
                            pathData.push(currData);
                        }

                        persistGeoFence([{
                                name: 'pathData',
                                value: JSON.stringify(pathData)
                            }]);
                    });
                });

                var geoListenerHandle = google.maps.event.addListener(geoFencingPositioningMap.map, 'tilesloaded', function () {
                    loadGeoFences();

                    google.maps.event.removeListener(geoListenerHandle);
                });
            }
        }
    }
}

function setupBarCodeTimer() {
    var myBarcodeLoadInterval = setInterval(function () {
        myTimer()
    }, 200);
    for (var i = 0; i < barcodesArray.length; i++) {
        barcodesArray[i].setMap(null);
    }
    barcodesArray = [];

    function myTimer() {
        barcodePositioningMap = PF('barcodePositioningMap');
        if (barcodePositioningMap != null) {
            if (barcodePositioningMap.getMap() != null) {
                
                clearInterval(myBarcodeLoadInterval);
            }
        }
    }
}

function setupBarCodeGPSTimer() {
    var myBarcodeGPSLoadInterval = setInterval(function () {
        myTimer()
    }, 200);
    for (var i = 0; i < barcodesGPSArray.length; i++) {
        barcodesGPSArray[i].setMap(null);
    }
    barcodesGPSArray = [];

    function myTimer() {
        googleMap = PF('googleMap');
        if (googleMap != null) {
            if (googleMap.getMap() != null) {
                var listenerHandle = google.maps.event.addListener(googleMap.map, 'tilesloaded', function () {
                    if (barcodesGPSArray == null || barcodesGPSArray.length == 0) {
                        //loadBarcodeCoordinatesOnMap();
                    }
                });
                clearInterval(myBarcodeGPSLoadInterval);
            }
        }
    }
}

function setBarcodeGPSData(barcodeDataVar) {
    if (barcodesArray) {
        for (var i = 0; i < barcodesArray.length; i++) {
            barcodesArray[i].setMap(null);
        }
    }
    if (barcodesGPSArray) {
        for (var i = 0; i < barcodesGPSArray.length; i++) {
            barcodesGPSArray[i].setMap(null);
        }
    }

    var shadowImage = new google.maps.MarkerImage('../images/non_scanned.png',
            new google.maps.Size(21, 34),
            new google.maps.Point(0, 0),
            new google.maps.Point(10, 34));

    for (var i = 0; i < barcodeDataVar.length; i++) {
        var barcodeLocation = barcodeDataVar[i];

        var myLatlng = new google.maps.LatLng(barcodeLocation['latitude'], barcodeLocation['longitude']);

        if (barcodePositioningMap != null && barcodePositioningMap.getMap() != null) {
            var marker = new google.maps.Marker({
                position: myLatlng,
                markerId: barcodeLocation['clientWaypointId'],
                title: barcodeLocation['waypointName'],
                shadow: shadowImage,
                //icon: 'http://chart.apis.google.com/chart?chst=d_text_outline&chld=FFCC33|16|h|FF0000|b|' + barcodeLocation.waypointName.subString(0, 1),
                draggable: true,
                animation: google.maps.Animation.DROP,
                map: barcodePositioningMap.map
            });
            google.maps.event.addListener(marker, 'dragend', dragChanged);
            barcodesArray.push(marker);
        }

        if (googleMap != null && googleMap.getMap() != null) {
            var newMarker = new google.maps.Marker({
                position: myLatlng,
                markerId: barcodeLocation['clientWaypointId'],
                title: barcodeLocation['waypointName'],
                animation: google.maps.Animation.DROP,
                shadow: shadowImage,
                //icon: 'http://chart.apis.google.com/chart?chst=d_text_outline&chld=FFCC33|16|h|FF0000|b|' + barcodeLocation.waypointName.substring(0, 1),
                map: googleMap.map
            });
            barcodesGPSArray.push(newMarker);
        }
    }
}

function loadMapCoordinates() {
    var dateStr = document.getElementById('trackingForm:tabView:dateSelect_input').value;
    var clientId = document.getElementById('trackingForm:clientId').value;
    var companyId = document.getElementById('trackingForm:companyId').value;
    var branchId = document.getElementById('trackingForm:branchId').value;
    $.ajax({
        type: 'GET',
        url: 'gpscoordinates?clientId=' + clientId + '&date=' + escape(dateStr) + '&branchId=' + branchId + '&companyId=' + companyId,
        data: '{}',
        success: setDeviceData
    });
}

function loadBarcodeCoordinatesOnMap() {
    var clientId = document.getElementById('trackingForm:clientId').value;
    var cid = document.getElementById('trackingForm:cid').value;
    var companyId = document.getElementById('trackingForm:companyId').value;
    var branchId = document.getElementById('trackingForm:branchId').value;
    $.ajax({
        type: 'GET',
        url: 'barcodegpscoordinates?clientId=' + clientId + '&branchId=' + branchId + '&companyId=' + companyId,
        data: '{}',
        success: setBarcodeGPSData
    });
}

function hasAnotherValue(value, val) {
    var index = parseInt(value, 10);

    index = (Math.floor(value / 60) * 100) + Math.floor(value % 60);

    var selectedLocationsArray = deviceData[index];
    for (var employeeId in selectedLocationsArray) {
        if (selectedLocationsArray[employeeId]) {
            var data = selectedLocationsArray[employeeId][currentIndex + val];
            if (selectedLocationsArray[employeeId][currentIndex + val] != null) {
                currentIndex = currentIndex + val;
                return true;
            }
        }
    }

    currentIndex = 0;
    return false;
}

function calcAngle(x1, x2, y1, y2)
{
    var calcAngle = Math.atan2(Math.abs(x1) - Math.abs(x2), Math.abs(y1) - Math.abs(y2)) * (180 / Math.PI);
    if (calcAngle < 0)
        calcAngle = Math.abs(calcAngle);
    else
        calcAngle = 360 - calcAngle;
    return calcAngle;
}

function checkIfEmployeeShouldBeTracking(employeeid) {
    if (!(employeeid in selectedEmployeeTracking)) {
        var checkbox = PF('employeeTracking_' + employeeid);
        if (typeof checkbox === 'undefined' || checkbox.length == 0) {
            return true;
        }
        var checkedVal = checkbox.isChecked();
        selectedEmployeeTracking[employeeid] = checkedVal;
    }
    return selectedEmployeeTracking[employeeid];
}

function fadeInPriorValues(value, num, employeeid) {
    var checkedVal = $("[id$=showHistoryChk_input]").is(':checked');

    if (checkedVal) {
        var index = parseInt(value, 10);
        index = (Math.floor(value / 60) * 100) + Math.floor(value % 60);

        var selectedLocationsArray = deviceData[index];
        var startIndex = currentIndex - 1;
        if (startIndex < 0) {
            startIndex = 0;
        }
        var lastLat = selectedLocationsArray[employeeid][currentIndex]['lat'];
        var lastLong = selectedLocationsArray[employeeid][currentIndex]['log'];
        for (var i = startIndex; i >= 0 && startIndex - i < Math.abs(num); i--) {
            var tempLocation = selectedLocationsArray[employeeid][i];
            var myLatlng = new google.maps.LatLng(tempLocation['lat'], tempLocation['log']);

            var angle = calcAngle(lastLong, tempLocation['log'], lastLat, tempLocation['lat']);

            lastLat = tempLocation['lat'];
            lastLong = tempLocation['log'];

            var marker = new google.maps.Marker({
                position: myLatlng,
                map: googleMap.map,
                icon: '../imageGPSServlet?angle=' + angle
            });

            if (!markersArray[employeeid]) {
                markersArray[employeeid] = new Array();
            }
            markersArray[employeeid].push(marker);
        }
    }
}

function fadeInNextvalues(value, num) {
    var index = parseInt(value, 10);
    index = (Math.floor(value / 60) * 100) + Math.floor(value % 60);

    var selectedLocationsArray = deviceData[index];
    var startIndex = currentIndex + 1;
    if (startIndex >= selectedLocationsArray.length) {
        startIndex = selectedLocationsArray.length - 1;
    }
    for (var i = startIndex; i < selectedLocationsArray.length && i - startIndex < Math.abs(num); i++) {
        var tempLocation = selectedLocationsArray[i];
        var myLatlng = new google.maps.LatLng(tempLocation['lat'], tempLocation['log']);

        var marker = new google.maps.Marker({
            position: myLatlng,
            map: googleMap.map,
            icon: '../imageGPSServlet?angle=' + angle
        });

        markersArray.push(marker);
    }
}

function toggleDisplay(employeeId, docTextId) {
    var mySel = PF(docTextId).isChecked();
    if (mySel) {
        for (var i = 0; i < markersArray[employeeId].length; i++) {
            markersArray[employeeId][i].setMap(googleMap.map);
        }
    } else {
        for (var i = 0; i < markersArray[employeeId].length; i++) {
            markersArray[employeeId][i].setMap(null);
        }
    }
}

function changeMap(value, index) {
    console.log('overlay popup!');
    var index = parseInt(value, 10);
    index = (Math.floor(value / 60) * 100) + Math.floor(value % 60);

    jQuery('#specificTime').val('');

    var selectedLocationsArray = deviceData[index];
    selectedEmployeeTracking = [];

    var centerVal = $("[id$=centerActivityChk_input]").is(':checked');
    var showAccuracy = $("[id$=showAccuracyChk_input]").is(':checked');
    if (infowindow != null) {
        infowindow.close();
    }
    for (var b = 0; b < barcodesGPSArray.length; b++) {
        barcodesGPSArray[b].setIcon('images/blue.png');
    }
    for (var c = 0; c < circlesArray.length; c++) {
        circlesArray[c].setMap(null);
    }
    circlesArray = [];

    var markers = googleMap.map.markers;
    if (markers) {
        for (var b = 0; b < markers.length; b++) {
            var marker = markers[b];
            if (marker.icon.indexOf('&isSel') > -1) {
                marker.setIcon(marker.icon.substring(0, marker.icon.indexOf('&isSel')));
            }
        }
    }

    if (markersArray) {
        //Removes all but the last entry for each employee if the time is the same, this way it won't flicker in and out on the map
        Object.keys(markersArray).forEach(function (employeeId) {
            for (var i = 0; i < markersArray[employeeId].length - 1; i++) {
                markersArray[employeeId][i].setMap(null);
            }
            if (markersArray[employeeId][markersArray[employeeId].length - 1]) {
                if (markersArray[employeeId][markersArray[employeeId].length - 1].myTime != value) {
                    markersArray[employeeId][markersArray[employeeId].length - 1].setMap(null);
                    markersArray[employeeId] = [];
                } else {
                    markersArray[employeeId] = [markersArray[employeeId][markersArray[employeeId].length - 1]];
                }
            }
        });
    }

    if (selectedLocationsArray != null) {
        for (var employeeId in selectedLocationsArray) {
            selectedLocations = selectedLocationsArray[employeeId][currentIndex];
            if (checkIfEmployeeShouldBeTracking(employeeId)) {
                if (selectedLocations != null) {
                    var myLatlng = new google.maps.LatLng(selectedLocations['lat'], selectedLocations['log']);
                    if (selectedLocations['isMarker']) {
                        if (markers) {
                            for (var b = 0; b < markers.length; b++) {
                                var marker = markers[b];
                                if (marker.getZIndex() == selectedLocations['markerId']) {
                                    marker.setIcon(marker.icon + '&isSel=true');
                                    jQuery('#specificTime').val('(' + marker.title + ') ' + selectedLocations['time']);
                                    if (centerVal) {
                                        googleMap.map.setCenter(barcodesGPSArray[b].position);
                                    }
                                }
                            }
                        }
                    } else {
                        var pinImage = new google.maps.MarkerImage(selectedLocations['iconURL'],
                                new google.maps.Size(21, 34),
                                new google.maps.Point(0, 0),
                                new google.maps.Point(10, 34));

                        //var image = '../images/User1.png';
                        var employeeName = selectedLocations['employeeName'];
                        var contentString = '<div class="phoneyheader">' + employeeName + '</div>' +
                                '<img class="phonyimage" src="' + selectedLocations['imageUrl'] + '"/>' +
                                '<div class="phoneytext">' +
                                '<div class="data_line">Equipment:</div><div class="data_d">' + selectedLocations['deviceNickname'] + "</div>" +
                                '<div class="data_line">Time:</div><div class="data_d">' + selectedLocations['time'] + "</div>" +
                                '<div class="data_line">Speed:</div><div class="data_d">' + selectedLocations['speed'] + "</div>" +
                                '<div class="data_line">Accuracy:</div><div class="data_d">' + selectedLocations['accuracy'] + "</div>" +
                                '</div>';
v
                        jQuery('#specificTime').val(selectedLocations['time']);

                        var marker = new google.maps.Marker({
                            position: myLatlng,
                            myContent: contentString,
                            title: selectedLocations['employeeName'],
                            map: googleMap.map,
                            zIndex: google.maps.Marker.MAX_ZINDEX + 1,
                            icon: pinImage,
                            myTime: value
                        });

                        if (showAccuracy) {
                            var circle = new google.maps.Circle({
                                center: myLatlng,
                                radius: selectedLocations['accuracy'] / 2,
                                strokeColor: "#000000",
                                strokeOpacity: 0.6,
                                strokeWeight: 3,
                                fillColor: "#FFFFFF",
                                fillOpacity: 0.5,
                                map: googleMap.map
                            });
                            circlesArray.push(circle);
                        }

                        infowindow = new InfoBubble({
                            maxWidth: 250,
                            minWidth: 250,
                            maxHeight: 120,
                            minHeight: 120,
                            shadowStyle: 1,
                            padding: 0,
                            backgroundColor: 'rgb(57,57,57)',
                            borderRadius: 4,
                            arrowSize: 10,
                            borderWidth: 1,
                            borderColor: '#2c2c2c',
                            disableAutoPan: true,
                            hideCloseButton: false,
                            arrowPosition: 30,
                            backgroundClassName: 'phoney',
                            arrowStyle: 0,
                            map: googleMap.map
                        });

                        if (centerVal) {
                            googleMap.map.setCenter(myLatlng);
                        }
                        google.maps.event.addListener(marker, 'click', function () {
                            infowindow.close();
                            infowindow.setContent(this.myContent);

                            infowindow.open(googleMap.map, marker);
                        });
                        if (!markersArray[employeeId] && employeeId != undefined) {
                            markersArray[employeeId] = new Array();
                        }
                        if (employeeId != undefined) {
                            //Strip out any existing entries left over by our previous step so that only one dot per employee will show
                            for (var i = 0; i < markersArray[employeeId].length; i++) {
                                markersArray[employeeId][i].setMap(null);
                                markersArray[employeeId] = new Array();
                            }
                            markersArray[employeeId].push(marker);
                        }

                        fadeInPriorValues(value, 4, employeeId);
                    }
                }
            }
        }
    }
}

function parseDeviceData(xhr, status, args) {
    setDeviceData(jQuery.parseJSON(args.gpsData));
//parse it, process it and load it into the chart
}

function setDeviceData(deviceDataVar) {
    deviceData = deviceDataVar;
    console.log(deviceData);
    setupSlider();
    hideLoadingBar();
}

function rewind() {
    window.clearInterval(interval);
    interval = setInterval("moveSelect(-1)", 300);
}

function forward() {
    window.clearInterval(interval);
    interval = setInterval("moveSelect(1)", 300);
}

function stop() {
    window.clearInterval(interval);
}

function back() {
    window.clearInterval(interval);
    interval = setInterval("moveSelect(-1)", 1000);
}

function play() {
    window.clearInterval(interval);
    interval = setInterval("moveSelect(1)", 1000);
}