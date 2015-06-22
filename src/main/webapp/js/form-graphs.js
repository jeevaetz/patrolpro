function fetchGraphData() {
    var companyId = $('#companyId').val();
    var clientId = $('#clientId').val();
    var startDate = $('#startDate').val();
    var endDate = $('#endDate').val();
    var lang = $('#lang').val();

    $.ajax({
        dataType: "json",
        url: '../LoadTotalReportUsageServlet?companyId=' + companyId + '&startDate=' + encodeURIComponent(startDate) + '&endDate=' + encodeURIComponent(endDate) + '&interval=day' + clientId + '&lang=' + lang,
        success: loadLineChartData
    });

    $.ajax({
        dataType: "json",
        url: '../LoadReportUsageBreakdownServlet?companyId=' + companyId + '&startDate=' + encodeURIComponent(startDate) + '&endDate=' + encodeURIComponent(endDate) + '&interval=day' + clientId + '&lang=' + lang,
        success: loadPieChartData
    });
    
    $.ajax({
        dataType: "json",
        url: '../LoadEmployeeReportUsageBreakdownServlet?companyId=' + companyId + '&startDate=' + encodeURIComponent(startDate) + '&endDate=' + encodeURIComponent(endDate) + '&interval=day' + clientId + '&lang=' + lang,
        success: loadEmployeePieChartData
    });
}

function fetchDeviceUsageData() {
    var companyId = $('#companyId').val();
    
    $.ajax({
        dataType: "json",
        url: '../../LoadDeviceCountsServlet?companyId=' + companyId,
        success: loadDeviceUsageData
    });
}

function resetStartEndDates() {
    $('#startDateCal').datepicker().on('changeDate', function (ev) {
        fetchGraphData();
    });

    $('#endDateCal').datepicker().on('changeDate', function (ev) {
        fetchGraphData();
    });

    fetchGraphData();
}

function loadPieChartData(data) {
    var chartOptions = {
        series: {
            pie: {
                show: true,
                innerRadius: 0,
                stroke: {
                    width: 4
                }
            }
        },
        legend: {
            show: false,
            position: 'ne'
        },
        tooltip: true,
        tooltipOpts: {
            content: '%s: %y.0'
        },
        grid: {
            hoverable: true
        },
        colors: mvpready_core.layoutColors
    }

    var holder = $('#piePanel')

    if (holder.length) {
        $.plot(holder, data.data, chartOptions)
    }
}

function loadEmployeePieChartData(data) {
    var chartOptions = {
        series: {
            pie: {
                show: true,
                innerRadius: 0,
                stroke: {
                    width: 4
                }
            }
        },
        legend: {
            show: false,
            position: 'ne'
        },
        tooltip: true,
        tooltipOpts: {
            content: '%s: %y.0'
        },
        grid: {
            hoverable: true
        },
        colors: mvpready_core.layoutColors
    }

    var holder = $('#employeePiePanel')

    if (holder.length) {
        $.plot(holder, data.data, chartOptions)
    }
}

function loadLineChartData(data) {
    var startDate = $('#startDate').val();
    startDate = startDate.replace(".", "/");
    var d1 = new Date(startDate);

    var endDate = $('#endDate').val();
    endDate = endDate.replace(".", "/");
    var d2 = new Date(endDate);


    var chartOptions = {
        xaxis: {
            min: d1.getTime(),
            max: d2.getTime(),
            mode: "time",
            tickSize: [2, "day"],
            tickLength: 2
        },
        yaxis: {
            min: 0
        },
        series: {
            lines: {
                show: true,
                fill: false,
                lineWidth: 3
            },
            points: {
                show: true,
                radius: 3,
                fill: true,
                fillColor: "#ffffff",
                lineWidth: 2
            }
        },
        grid: {
            hoverable: true,
            clickable: false,
            borderWidth: 0
        },
        legend: {
            show: true
        },
        tooltip: true,
        tooltipOpts: {
            content: '%s (%x - %y.0)'
        },
        colors: mvpready_core.layoutColors
    }

    var holder = $('#graphPanel')

    if (holder.length) {
        $.plot(holder, data.data, chartOptions)
    }
}


function loadDeviceUsageData(data) {
    var startDate = new Date();
    startDate.setMonth(startDate.getMonth() - 6);
    
    var endDate = new Date();


    var chartOptions = {
        xaxis: {
            min: startDate.getTime(),
            max: endDate.getTime(),
            mode: "time",
            tickSize: [14, "day"],
            tickLength: 2
        },
        yaxis: {
            min: 0
        },
        series: {
            lines: {
                show: true,
                fill: false,
                lineWidth: 3
            },
            points: {
                show: true,
                radius: 3,
                fill: true,
                fillColor: "#ffffff",
                lineWidth: 2
            }
        },
        grid: {
            hoverable: true,
            clickable: false,
            borderWidth: 0
        },
        legend: {
            show: true
        },
        tooltip: true,
        tooltipOpts: {
            content: '%s (%x - %y.0)'
        },
        colors: mvpready_core.layoutColors
    }

    var holder = $('#graphPanel')

    if (holder.length) {
        $.plot(holder, data.data, chartOptions)
    }
}