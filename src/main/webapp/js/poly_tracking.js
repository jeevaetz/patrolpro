var interval = ""
var deviceData = new Array();
var googleMap;
var barcodePositioningMap;
var markersArray = [];
var barcodesArray = [];
var barcodesGPSArray = [];
var infowindow;
var projection;
var overlayObj;
var currentIndex = 0;
          

function moveSelect(val) {
    var timeLineVar = $('select[name=timeLine]');
    if (hasAnotherValue(timeLineVar.val(), val)) {
        changeMap(timeLineVar.val(), currentIndex);
    } else {
        timeLineVar.prop("selectedIndex", timeLineVar.prop("selectedIndex") + val);
        timeLineVar.trigger('click');
        changeMap(timeLineVar.val(), currentIndex);
    }
}
       
function setupBarcodeMap() {
    
}

function handlePointClick(event) {  
    $("[id*=':lat']").val(event.latLng.lat());  
    $("[id*=':lng']").val(event.latLng.lng());  

    dlg.show();    
} 
       
function setupSlider() {
    $(".ui-slider").remove();
                        
    $(function(){
        $('select[name=timeLine]').selectToUISlider({
            sliderOptions: {
                stop: function(e,ui) {
                    changeMap($('select[name=timeLine]').val());
                },
                highlightvalues: deviceData
            },
            labels: 12
        }).hide();
    });
}

/**
 * This function is called when a barcode marker is dropped after a drag.
 */
function dragChanged(event) {
    var eventUrl = 'updatebarcodegpsservlet?waypointid=' + this.markerId + '&lat=' + event.latLng.lat() + '&lng=' + event.latLng.lng();
    $.ajax({
        type: 'GET',
        url: eventUrl,
        data: '{}',
        success: loadBarcodeCoordinatesOnMap
    });
}

function setupBarCodeTimer() {
    var myBarcodeLoadInterval = setInterval(function(){
        myTimer()
    }, 200);
    for (var i = 0; i < barcodesArray.length; i++) {
        barcodesArray[i].setMap(null);
    }
    barcodesArray = [];

    function myTimer() {
        if (barcodePositioningMap != null) {
            if (barcodePositioningMap.map != null) {
                google.maps.event.addListener(barcodePositioningMap.map, 'tilesloaded', function(){
                    if (barcodesArray == null || barcodesArray.length == 0) {
                        loadBarcodeCoordinatesOnMap();
                    }
                });
                clearInterval(myBarcodeLoadInterval);
            }
        }
    }
}
                    
function setupBarCodeGPSTimer() {
    var myBarcodeGPSLoadInterval = setInterval(function(){
        myTimer()
    }, 200);
    for (var i = 0; i < barcodesGPSArray.length; i++) {
        barcodesGPSArray[i].setMap(null);
    }
    barcodesGPSArray = [];

    function myTimer() {
        if (googleMap != null) {
            if (googleMap.map != null) {
                google.maps.event.addListener(googleMap.map, 'tilesloaded', function(){
                    if (barcodesGPSArray == null || barcodesGPSArray.length == 0) {
                        loadBarcodeCoordinatesOnMap();
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
    
    for (var i = 0; i < barcodeDataVar.length; i++) {
        var barcodeLocation = barcodeDataVar[i];
        
        var myLatlng = new google.maps.LatLng(barcodeLocation['latitude'], barcodeLocation['longitude']);
        
        if (barcodePositioningMap != null && barcodePositioningMap.map != null) {
            var marker = new google.maps.Marker({
                position: myLatlng,
                markerId: barcodeLocation['clientWaypointId'],
                title: barcodeLocation['waypointName'],
                draggable: true,
                animation: google.maps.Animation.DROP,
                map: barcodePositioningMap.map
            });
            google.maps.event.addListener(marker, 'dragend', dragChanged); 
            barcodesArray.push(marker);
        }
        
        if (googleMap != null && googleMap.map != null) {
            var newMarker = new google.maps.Marker({
                position: myLatlng,
                markerId: barcodeLocation['clientWaypointId'],
                title: barcodeLocation['waypointName'],
                animation: google.maps.Animation.DROP,
                map: googleMap.map
            });
            barcodesGPSArray.push(newMarker);
        }
    }
}

function loadMapCoordinates() {
    var dateStr = document.getElementById('tabForm:tabView:dateSelect_input').value;
    var clientId = document.getElementById('tabForm:clientId').value;
    $.ajax({
        type: 'GET',
        url: 'gpscoordinates?clientId=' + clientId + '&date=' + escape(dateStr),
        data: '{}',
        success: setDeviceData
    });
}

function loadBarcodeCoordinatesOnMap() {
    var clientId = document.getElementById('tabForm:clientId').value;
    $.ajax({
        type: 'GET',
        url: 'barcodegpscoordinates?clientId=' + clientId,
        data: '{}',
        success: setBarcodeGPSData
    });
}
      
function hasAnotherValue(value, val) {
    var index = parseInt(value, 10);
    var selectedLocationsArray = deviceData[index];
    if (selectedLocationsArray != null) {
        var data = selectedLocationsArray[currentIndex + val];
        if (selectedLocationsArray[currentIndex + val] != null) {
            currentIndex = currentIndex + val;
            return true;
        }
    }
    currentIndex = 0;
    return false;
}
      
function calcAngle(x1, x2, y1, y2)
{
    var calcAngle = Math.atan2(Math.abs(x1)-Math.abs(x2),Math.abs(y1)-Math.abs(y2))*(180/Math.PI);
    if(calcAngle < 0)
        calcAngle = Math.abs(calcAngle);
    else
        calcAngle = 360 - calcAngle;
    return calcAngle;
}
      
function fadeInPriorValues(value, num) {
    var index = parseInt(value, 10);
    var selectedLocationsArray = deviceData[index];
    var startIndex = currentIndex - 1;
    if (startIndex < 0) {
        startIndex = 0;
    }
    var lastLat = selectedLocationsArray[currentIndex]['lat'];
    var lastLong = selectedLocationsArray[currentIndex]['log'];
    for (var i = startIndex; i >= 0 && startIndex - i < Math.abs(num); i--) {
        var tempLocation = selectedLocationsArray[i];
        var myLatlng = new google.maps.LatLng(tempLocation['lat'], tempLocation['log']);
        
        var angle = calcAngle(lastLong, tempLocation['log'], lastLat, tempLocation['lat']);
        
        lastLat = tempLocation['lat'];
        lastLong = tempLocation['log'];
        
//        var marker = new google.maps.Marker({
//            position: myLatlng,
//            map: googleMap.map,
//            icon: '../imageGPSServlet?angle=' + angle
//        });
//        
//        markersArray.push(marker);
    }
}

function overlay(tile, proj) {
    proj = proj(tile);
    var tl = proj.locationPoint({
        lon: 32.3107, 
        lat: -89.6761
    }),
    br = proj.locationPoint({
        lon: -122.375, 
        lat: 37.755
    }),
    image = tile.element = po.svg("image");
    image.setAttribute("preserveAspectRatio", "none");
    image.setAttribute("x", tl.x);
    image.setAttribute("y", tl.y);
    image.setAttribute("width", br.x - tl.x);
    image.setAttribute("height", br.y - tl.y);
    image.setAttributeNS("http://www.w3.org/1999/xlink", "href", "sf1906.png");
}

function fadeInNextvalues(value, num) {
    var index = parseInt(value, 10);
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
      
function changeMap(value, index) {
    $('span[name=currentTime]').html($('select[name=timeLine]').val());
    var index = parseInt(value, 10);
    var selectedLocationsArray = deviceData[index];
    if (infowindow != null) {
        infowindow.close();
    }
    if (selectedLocationsArray != null) {
        if (markersArray) {
            for (var i = 0; i < markersArray.length; i++ ) {
                map.remove(markersArray[i]);
            }
        }
        selectedLocations = selectedLocationsArray[currentIndex];
        if (selectedLocations != null) {
            var myLatlng = new google.maps.LatLng(selectedLocations['lat'], selectedLocations['log']);
             
            var contentString = '<div class="phoneyheader">' + selectedLocations['employeeName'] + '</div>' +
            '<div class="phoneytext">Time: ' + selectedLocations['time'] + '</div>' +
            '<div class="phoneytext">Accuracy: ' + selectedLocations['accuracy'] + '</div>' +
            '<img class="phonyimage" src="' + selectedLocations['imageUrl'] + '"/>';
        
            var image = '../images/User1.png';
             
            //            infowindow = new InfoBubble({
            //                content: contentString,
            //                maxWidth: 200,
            //                minWidth: 200,
            //                maxHeight: 120,
            //                minHeight: 120,
            //                shadowStyle: 1,
            //                padding: 0,
            //                backgroundColor: 'rgb(57,57,57)',
            //                borderRadius: 4,
            //                arrowSize: 10,
            //                borderWidth: 1,
            //                borderColor: '#2c2c2c',
            //                disableAutoPan: true,
            //                hideCloseButton: true,
            //                arrowPosition: 30,
            //                backgroundClassName: 'phoney',
            //                arrowStyle: 0,
            //                map: googleMap.map
            //            });
            //            
            //            var marker = new google.maps.Marker({
            //                position: myLatlng,
            //                title: selectedLocations['employeeName'],
            //                map: googleMap.map,
            //                icon: image
            //            });
            //            
            //            googleMap.map.setCenter(myLatlng);
            //            
            //            google.maps.event.addListener(marker, 'click', function() {
            //                infowindow.open(googleMap.map, marker);
            //            });

            if (!isNaN(selectedLocations['lat']) && !isNaN(selectedLocations['log'])) {
                var marker = po.geoJson()
                    .features([{
                        "geometry":{
                            "coordinates": [
                                selectedLocations['log'], selectedLocations['lat']
                            ], 
                            "type": "Point"
                        }
                    }]).id("current_spot");
                map.add(marker);
                map.center({"lat":selectedLocations['lat'], "lon":selectedLocations['log']});
                markersArray.push(marker);
            }
            
        fadeInPriorValues(value, 4);
    }
}
}
                    
function setDeviceData(deviceDataVar) {
    deviceData = deviceDataVar;
    setupSlider();
    hideLoadingBar();
}
                    
function rewind() {
    window.clearInterval(interval);
    interval = setInterval( "moveSelect(-1)", 500 );
}
                    
function forward() {
    window.clearInterval(interval);
    interval = setInterval( "moveSelect(1)", 500 );
}
                    
function stop() {
    window.clearInterval(interval);
}
                    
function back() {
    window.clearInterval(interval);
    interval = setInterval( "moveSelect(-1)", 2000 );
}
                    
function play() {
    window.clearInterval(interval);
    interval = setInterval( "moveSelect(1)", 2000 );
}