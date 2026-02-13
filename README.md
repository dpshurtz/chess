# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2AMQALADMABwATG4gMP7I9gAWYDoIPoYASij2SKoWckgQaJiIqKQAtAB85JQ0UABcMADaAAoA8mQAKgC6MAD0PgZQADpoAN4ARP2UaMAAtihjtWMwYwA0y7jqAO7QHAtLq8soM8BICHvLAL6YwjUwFazsXJT145NQ03PnB2MbqttQu0WyzWYyOJzOQLGVzYnG4sHuN1E9SgmWyYEoAAoMlkcpQMgBHVI5ACU12qojulVk8iUKnU9XsKDAAFUBhi3h8UKTqYplGpVJSjDpagAxJCcGCsyg8mA6SwwDmzMQ6FHAADWkoGME2SDA8QVA05MGACFVHHlKAAHmiNDzafy7gjySp6lKoDyySIVI7KjdnjAFKaUMBze11egAKKWlTYAgFT23Ur3YrmeqBJzBYbjObqYCMhbLCNQbx1A1TJXGoMh+XyNXoKFmTiYO189Q+qpelD1NA+BAIBMU+4tumqWogVXot3sgY87nae1t+7GWoKDgcTXS7QD71D+et0fj4PohQ+PUY4Cn+Kz5t7keC5er9cnvUexE7+4wp6l7FovFqXtYJ+cLtn6pavIaSpLPU+wgheertBAdZoFByyXAmlDtimGD1OEThOFmEwQZ8MDQcCyxwfECFISh+xXOgHCmF4vgBNA7CMjEIpwBG0hwAoMAADIQFkhRYcwTrUP6zRtF0vQGOo+RoARiqfJCIK-P8gK0eh8KVEB-rgeWKkwes+h-DsXzQo8wHiVQSIwAgQnihignCQSRJgKSb6GLuNL7gyTJTspXI3r5d5LsKMBihKboynKZbvEqIW8mFNl2TFW4qsGGoAJJoCA0AouAMDqTsMDQPFRocBAahoAA5MwVpotu3k2f6zLTJe0BIAAXigHAZDYBQGNpmHIKmMDpgAjAROaqHm8zQUWJb1D4HV6l1vW7HRTZeYKw78mOE4oM+8Tnpe177YulQPmuAbnVuu1JrpVn+i54oDQBmB6fCrVgYRhnzCRqHfBRVH1kDWmgaNJRgDheFKURgOkSDl5g8hENofRjHeH4-heCg6AxHEiT44TLm+FgomCqB9QNNIEb8RG7QRt0PRyaoCnDKDiHoCNH4vaW3NIV9AvU86XbGqjPOKWgDn2BTGKeeLe23gdRgoNwx6Xmd8HS3OoUOuF9TSBrTKGCdsXykLvOZeqku60hMDZE7aBUCaSAMZdApPR2tkujAABmJoGDAGIcKb6IKmgqg+PuAe9krnYgdUbVrfEG19QN2BDWIUM+6JaZONNoxjLN80FmMS3QCtacZ1tjae6ri6pf7FipKH4ea4YMdxwnzXtt99Rt2I33JxJYFXHnyZjdhMC4fhozbQxng4wEKLrv42Dihq-FojAADiSoaFTv20-vTOs-YSpc1Lwt589sL+tbhSjy3EvP8MyA5IfOaK-3PnJTVoyMAJ0daUT1klBc3troRX3kyO6L5tCyitrfG2qoNTPydgKcUbtkCNwNs3X04tuy9n7I9U+kpa5QB6pnNQ2do65xTj9SoBcJpFxmvycui1izVyjhROuDYsZezHn7CWFsHrKwAVA+oX8wA-zUH-YRRsD7wJNAgA+SoPRKLfvUOAEA+woHAApAAPPInk5R-780fqWXe38j4fQQIBUWPsaYwHGFfHMBYGjuKVNlaQBZJrhGCIEEEmx4i6hQG6TkXwQTJFAGqKJkFVLLA8SgAAckqSEFwYCdEnkw6G4154EVSaoLxPi5h+ICUEkJywwkRMSUZMiYw4kgASYjGJKSlQZLmFknJS9sbMX8BwAA7G4JwKAnAxAjMEOAXEABs8AjoaLmDAIoM8xJEPHrTVoHRL7XzTmjYpXSkmQ3yVYr89QeipJvg7dARy5jdIWlpV+mzRH1A-nlI68iMSHjkCgeRbk1AeX-lSJuo4YDANAc-fWgCrpCnqHA5g4j5DIPtuA4WtsMGoLQFgl2uCPaQP3CIuyPY+yWNeanfh1DNpZxznzaeMNC7F2zJw-M3Dlp8M6tSvqgimzaNeXZZFwAQUyDBfUX56JvmpMeTCqB94IpwCWWgFAmxlkoEthC45cxyUPGsbor5SoHFOOsSI-SYxUmVPqIE4JeTx4FNnkUkuFr-FWuqf0legzLAawcqq2ISAEhgC9X2CAqqABSEBxRqpiC0tUayYZiy2Y0JozIZJXP2R88Y2AEDAC9VAPRDkoAdPNb4l1py7XnLhPUG56K7klyzTmyg+boBFudRZEWJqdFosOWgAAVhGtA3zw3vRQISIFidREqwIeCyF2toWErCjAhF8ChWoufpgTFXbpa4pwe7fBsLvadtJWQyRFD2pUpof1OhdKp4sPWUyjhuY2WFh4aWVa57Nq8r3XKlxxCEFXgkUnKRfkIVMilSW2VRLlGIrVRq5187DadrMRldBMBAW4nXPlAtMBay83IRS0sIpoA6A9uHNAtKGGhxRKO9DMBMPlTQBAZgR61jlVSUYaqAoGMNWtCSelt7GVsMzCXMuT7K4vveVWc02HwzIX6Y9UFU6VrYC0JKpUGJlWbCQ-ICDC74WRXFOuNjcVnXGglCiet3odR6hgBpowSCMRWf1LZoKxoBR6kMCAdOBgcV0agOOwcFb-Ttq-KaiefH4B3rnvDReDcBm4y8Dmom-qSZQES4gYMsBgDYCzYQPIBRVkn3w7TemjNmas2MHzB+FzgvWQFf7EA3A8B-3k6KxTtHGtQEUWC+VxsI6GDURud02g1haeAGsR4f7Xwnqq5W1Zr8KEjFtYmBlhTouLYbkAA)
## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
