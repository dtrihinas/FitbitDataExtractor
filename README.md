# FitbitDataExtractor

extract YOUR own raw data (steps, heartrate, calories, active minutes, distance) from fitbit

How to run
----------
import FitbitDataExtractor and use as arguments (args) your email and password to get YOUR own data

Examples
---------
```java
    		//authenticate
		FitbitDataExtractor extractor = new FitbitDataExtractor();
		extractor.getAuthenticated(args[0], args[1]);

		//get heartrate and calories for specific data and store as csv
		extractor.getIntraDayHeartandCalories("2016-01-31", true);
		
		//get steps for specific data and store as csv
		extractor.getIntraDaySteps("2016-01-31", true);
		
		//get steps for a whole month and store as csv files per data type
		for (int i = 1; i <= 31; i++)
			extractor.getIntraDaySteps("2016-03-" + i, true);

		//get everything for a whole month!
		for (int i = 1; i <= 31; i++) {
			extractor.getIntraDaySteps("2016-07-" + i, true);
			extractor.getIntraDayCalories("2016-07-" + i, true);
			extractor.getIntraDayFloors("2016-07-" + i, true);
			extractor.getIntraDayActiveMins("2016-07-" + i, true);
			extractor.getIntraDayDistance("2016-07-" + i, true);
		}
    
```

Disclaimer
----------
The extractor is to get YOUR own data and was created and used solely for academic research. As fitbit changes its API the extractor may not work and may need some e.g. parameter changes (for changes please ask nicely!)

Data
-----
Get your own data with the extractor!
or... send us an email (trihinas{at}cs.ucy.ac.cy) if you are working on a research project and are in need of data
