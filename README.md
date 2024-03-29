# job-interview-task-Weatherbit-Forecast

## Task Details:

![Screenshot](job-interview-task-Weatherbit-Forecast-Task-Details.png)


## Requirements

- Java 17
- Maven (for building and running the application)

## Setup and Installation

1. **Clone the repository**

    ```shell
    git clone https://github.com/rmaduzia/job-interview-task-Weatherbit-Forecast
    cd job-interview-task-Weatherbit-Forecast
    ```

2. **Configure the Weatherbit API Key**

   Before running the application, you must provide your Weatherbit API key. This key should be passed as a system variable.

   If you do not have a Weatherbit API key, sign up at [Weatherbit.io](https://www.weatherbit.io/) to obtain one.

3. **Build the Application with Maven**

    ```shell
    mvn clean install
    ```

   This command compiles the application and generates an executable .jar file in the `target` directory.

## Running the Application

To run the application, use the following command, replacing `YOUR_API_KEY_HERE` with your actual Weatherbit API key:

```shell
java -DapiKey=YOUR_API_KEY_HERE -jar target/weatherbit-0.0.1-SNAPSHOT.jar
```
## Heroku
Applicaiton is deployed in heroku:

https://job-interview-task-weatherbit-0738528c493e.herokuapp.com/weather?date=2024-03-19


