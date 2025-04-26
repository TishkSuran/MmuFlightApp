# Flight Punctuality Tracker - README

## Student: Tishk Suran

Hello Dr. Welsh, or other professor who may be marking this. 

This is my submission for the Advanced Programming coursework.

## How to run the application

There are two main programs in the submission:

1. **DataImportMain.java** - This imports the CSV file into the SQLite database
2. **FlightApp.java** - This is the main desktop application for searching and analysing the data

To run the application:
- Run DataImportMain first to create and populate the database
- Then run FlightApp to view and analyse the data

## Project Structure

- **src/database/** - Contains classes for database connection and CSV import.
- **src/flightModel/** - Contains the Flight class and related models.
- **src/service/** - Contains the data access service layer.
- **src/ui/** - Contains all the UI components and panels.
- **screenshots/** - Contains the requested screenshots.
- **lib/** - Contains the required external libraries.

## Features Implemented

### Data Import Program (DataImportMain.java)
- Creates SQLite database with correct schema as per specification.
- Imports all flight data from the CSV.
- Handles error checking and skips invalid rows.
- Creates indices for improved query performance.
- Displays progress during import.

### Desktop Application (FlightApp.java)
- **Basic Search & Display Functionality**
  - Search by airline, flight number, origin/destination airport.
  - Search by date range.
  - Search by delay duration and reaso.n
  - View detailed flight information when a row is selected.

- **Analysis & Reporting**
  - View average delay by airline for a specific year.
  - View average delay by airport for a specific year.
  - View delay trends over time for a specific airport.
  - All charts use JFreeChart as required.

- **UI Features**
  - Responsive layout that resizes properly.
  - Sortable tables (click column headers).
  - Airport and airline codes shown with names for better usability.
  - Split pane interface for resizable results vs details.

## Known Issues & Limitations

- The application sometimes runs a bit slow when doing complex queries across the whole dataset. I tried to optimise this with indices but there might be room for improvement.
- The time series chart can look a bit crowded when showing many months of data.
- There's a weird bug where occasionally the date selector doesn't register the first click - you have to click twice.

## Database Design Decisions

For performance reasons, I created the following indices:
- Flight.airline_code
- Flight.flight_origin, Flight.flight_destination
- Flight.date
- Delay_Reason.flight_id
- Delay_Reason.reason

Thank you for your time reviewing this assignment. I'm happy to answer any questions or explain any part of the implementation.
