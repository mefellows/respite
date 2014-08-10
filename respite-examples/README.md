# Respite Examples #

This project showcases the standard features of Respite and provides an example template for new projects.
 
## Prerequisites

A working local MongoDB installation.

If running on a port other than 17017 (including the default 27017) create a [.env](https://github.com/mefellows/sbt-dotenv) file in the base project dir, with the following entry:
 
```sh
MONGO_PORT=27017
```

## Build & Run ##

```sh
$ git clone git@github.com:mefellows/respite.git && cd respite
$ sbt
> container:start
> browse
```

If `browse` doesn't launch your browser, manually open [http://localhost:8080/](http://localhost:8080/) in your browser.

## API Tests

### POSTMan

If you like to use the excellent [POSTman](http://www.getpostman.com/) application, import the ```respite-examples-postman.json``` file and start clicking!
 
### cURL

Create a User

```sh
curl -v -X POST -H"content-type: application/json" -H"x-api-key: murray" -H"x-api-application: bill" "http://localhost:8080/users/" -d \
'{"username":"cpeebody", "firstName":"Crabapple", "lastName":"Peebody", "dob":"2000-01-04", "password":"bar" }'
```

Get Users:

```sh
curl -X GET -H"content-type: application/json" -H"x-api-key: murray" -H"x-api-application: bill" "http://localhost:8080/users/"
```

See API Metrics:

```sh
curl -X GET -H"content-type: application/json" -H"x-api-key: murray" -H"x-api-application: bill" "http://localhost:8080/metrics/"
```

Check API Health:

```sh
curl -X GET -H"content-type: application/json" -H"x-api-key: murray" -H"x-api-application: bill" "http://localhost:8080/metrics/health"
```


