# WebKIndex

WebKIndex is an Apache Maven web application project written in JAVA which makes use of the Spring Boot framework. The external dependencies integrated in this project are mentioned in the "pom.xml" file. 
The project includes two types of web services: a) services called by the Knowledge Integration component and b) functions that run in predefined time intervals. 
Main services and functions supported: 
- Tweet, passage, newspaper and recipes retrieval
- Relation extraction
- Topic detection (R scripts called by this service are included)
- KRISTINA-tailored location-based search services (nearest places, events, weather forecasts)
- German newspaper crawling 
- Category classification service (runs the python scripts on the "KRISTINA/CategoryClassification" subfolder)
  
# Version
1.0.0