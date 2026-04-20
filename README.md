# OpResearch-CARRIBA_NOSRATI

Project for the Operations Research course at Université Côte d'Azur.

General information about the project, its goal and how it works, can be found in `report/Rapport.pdf`.

## Author

Daniel Carriba Nosrati

## Requirements

- Java is required <br>
  Java version 21 is highly recommended, as the project was build and tested with Java 21

- Gradle Wrapper is included, no separate Gradle installation is required.

## Clone the repository

```bash
git clone https://github.com/dcarriba/OpResearch-CARRIBA_NOSRATI.git
```

## Build and Run Instructions

Navigate into the project directory:

```bash
cd OpResearch-CARRIBA_NOSRATI
```

### Important notice

Make sure the `gradlew` file has the rights to be executed on your machine.

If you are on Windows using CMD or PowerShell, you may use `gradlew.bat` instead of `./gradlew` for all following commands.

### Build the project

To build the project, use the following command:

```bash
./gradlew build
``` 

### Run the project

To run the project, use the following command:

```bash
./gradlew run --args="path/to/input.txt"
```

With `path/to/input.txt` the input file containing the graphs data.

### Run unit tests

To run the unit tests of the project, use the following command

```bash
./gradlew test
```
