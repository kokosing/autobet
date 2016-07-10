# autobet [![Build Status](https://travis-ci.org/kokosing/autobet.svg?branch=master)](https://travis-ci.org/kokosing/autobet)

## Running unit tests in Intellij

See [activejdbc intellij integration](http://javalite.io/intellij_idea_integration).

Perform these steps:

  * Run -> Edit configurations -> Defaults -> JUnit 
  * Enter this as a post-Make step maven goal:

      org.javalite:activejdbc-instrumentation:[VERSION]:instrument
