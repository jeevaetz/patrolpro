var x = require('casper').selectXPath;
casper.options.viewportSize = {width: 1920, height: 943};
casper.on('page.error', function(msg, trace) {
   this.echo('Error: ' + msg, 'ERROR');
   for(var i=0; i<trace.length; i++) {
       var step = trace[i];
       this.echo('   ' + step.file + ' (line ' + step.line + ')', 'ERROR');
   }
});

//var server = 'http://localhost';
var server = 'http://www.patrolpro.com';

casper.test.begin('Resurrectio test', function(test) {
   casper.start(server + '/login.xhtml');
   casper.waitForSelector(".masthead",
       function success() {
           test.assertExists(".masthead");
           this.click(".masthead");
       },
       function fail() {
           test.assertExists(".masthead");
   });
   casper.waitForSelector(x('//button[@type = "submit"]/span[.="Login"]'),
       function success() {
           test.assertExists(x('//button[@type = "submit"]/span[.="Login"]'));
           this.fill('form[name="loginForm"]', {'loginForm:username' : 'rharris', 'loginForm:password' : 'rharris'}, false);
           this.click(x('//button[@type = "submit"]/span[.="Login"]'));
       },
       function fail() {
           test.assertExists(x('//button[@type = "submit"]/span[.="Login"]'));
   });
   /* Validate Send Link Button is there */
   casper.waitForSelector("a[href = '#sendLinkModal']",
       function success() {
		   console.log("URL" + this.getCurrentUrl());
           test.assertExists("a[href = '#sendLinkModal']");
       },
       function fail() {
           test.assertExists("a[href = '#sendLinkModal']");
   });
   /* validate client tab is there and click it */
   casper.waitForSelector(x('//a[starts-with(@href, "admin_client.xhtml")]'),
       function success() {
           test.assertExists(x('//a[starts-with(@href, "admin_client.xhtml")]'));
           this.click(x('//a[starts-with(@href, "admin_client.xhtml")]'));
       },
       function fail() {
           test.assertExists(x('//a[starts-with(@href, "admin_client.xhtml")]'));
   });
   /* Wait for BET info to load and validate then click */
   casper.waitForSelector(x("//td[contains(., 'BET Corp Office')]"),
       function success() {
           test.assertExists(x("//td[contains(., 'BET Corp Office')]"));
           this.click(x("//td[contains(., 'BET Corp Office')]"));
       },
       function fail() {
           test.assertExists(x("//td[contains(., 'BET Corp Office')]"));
   });
   /* wait for window to load, validate client info shows, close it */
   casper.waitForSelector(x("//input[@value = '1235 W. Street NE']"),
          function success() {
              test.assertExists(x("//input[@value = '1235 W. Street NE']"));
              this.click('div[id="ClientInfoModal"] button[class="close"]');
          },
          function fail() {
              test.assertExists(x("//input[@value = '1235 W. Street NE']"));
   });
   /* Change to Lithuanian */
   casper.waitForSelector(x("//a[text()='Lithuanian']"),
       function success() {
           test.assertExists(x("//a[text()='Lithuanian']"));
           this.click(x("//a[text()='Lithuanian']"));
       },
       function fail() {
           test.assertExists(x("//a[text()='Lithuanian']"));
   });

   //Validate Lithuanian with special UTF-8 character encoding
   casper.waitForSelector(x("//p[text()='Maršrutai']"),
	       function success() {
	           test.assertExists(x("//p[text()='Maršrutai']"));
	           this.click(x("//a[text()='English']"));
	       },
	       function fail() {
	           test.assertExists(x("//p[text()='Maršrutai']"));
   });

   //Validate back in english, and switch pages
   casper.waitForSelector(x("//p[text()='Client Data']"),
   	       function success() {
   	           test.assertExists(x("//p[text()='Client Data']"));
   	           this.click(x('//a[starts-with(@href, "admin_employee.xhtml")]'));
   	       },
   	       function fail() {
   	           test.assertExists(x("//p[text()='Client Data']"));
   });

   //Switch Branch
   casper.waitForSelector(x("//a[text()='tests']"),
      	       function success() {
      	           test.assertExists(x("//a[text()='tests']"));
      	           this.click(x("//a[text()='tests']"));
      	       },
      	       function fail() {
      	           test.assertExists(x("//a[text()='tests']"));
   });

   //Look for specific employee and click on td
   casper.waitForSelector(x("//td[contains(., 'Apt 248 1001 Lake Carolyn Pkwy')]"),
       function success() {
           test.assertExists(x("//td[contains(., 'Apt 248 1001 Lake Carolyn Pkwy')]"));
           this.click(x("//td[contains(., 'Apt 248 1001 Lake Carolyn Pkwy')]"));
       },
       function fail() {
           test.assertExists(x("//td[contains(., 'Apt 248 1001 Lake Carolyn Pkwy')]"));
   });

   /* wait for window to load, validate client info shows, close it */
   casper.waitForSelector(x("//input[@value = 'Apt 248 1001 Lake Carolyn Pkwy']"),
          function success() {
              test.assertExists(x("//input[@value = 'Apt 248 1001 Lake Carolyn Pkwy']"));
              this.click('div[id="EmployeeModal"] button[class="close"]');
          },
          function fail() {
              test.assertExists(x("//input[@value = 'Apt 248 1001 Lake Carolyn Pkwy']"));
   });

   //Click Add Employee
   casper.waitForSelector(x("//a[text()='Add Employee']"),
   		  function success() {
			  test.assertExists(x("//a[text()='Add Employee']"));
	  		  this.click(x("//a[text()='Add Employee']"));
	  	  },
	  	  function fail() {
			  test.assertExists(x("//a[text()='Add Employee']"));
   });

   //Check that blank employee loads
   casper.waitForSelector(x("//*[@id='AdminDashboardForm:firstName' and @value = '']"),
          function success() {
              test.assertExists(x("//*[@id='AdminDashboardForm:firstName' and @value = '']"));
              this.click(x("//a[contains(text(), 'Save')]/@href"));
          },
          function fail() {
              test.assertExists(x("//*[@id='AdminDashboardForm:firstName' and @value = '']"));
   });

   //Check that there is an error for SSN - Blank SSN is checked.
   casper.waitForSelector(x("//*[@class='ui-messages-warn-detail' and text() = 'Please enter a SSN!']"),
          function success() {
              test.assertExists(x("//*[@id='AdminDashboardForm:firstName' and @value = '']"));
              this.click(x("//a[text()='Save']]"));
          },
          function fail() {
              test.assertExists(x("//*[@id='AdminDashboardForm:firstName' and @value = '']"));
   });

   casper.open(server + '/PrintMobileFormServlet?companyId=5095&mobileFormFilloutId=42892');

   casper.run(function() {test.done();});
});