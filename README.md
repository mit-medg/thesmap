thesmap
=======

Instructions for Setup 

1. Eclipse Luna
2. JDK 8 - had an issue with JSE 7  - unbound error; needed to make sure that the right jar was in place 
3. Checkout this repo from github.
4. Add .ThesMaps.properties file in the home directory
    - Update to use the proper servers for MetaMap and LVG
5. Install LVG - need to run install_mac.sh - should use the latest version of lvg
6. Installed Metamap (https://github.com/licongcui/documentation/blob/master/metamap-installation.md)
    - relocate to /usr/local/metamap/public_mm
    Start the SKR/Medpost Tagger: ./bin/skrmedpostctl start
    Start the Word Sense Disambiguation (WSD) Server:./bin/wsdserverctl start
    Start the MetaMap server: ./bin/mmserver<year>
7. Install cTAKES
  Steps for installation: https://docs.google.com/document/d/1Mi3uvaOBt9tywWOAZQDbrAxqV3lZNMFT1LrQsfco8uo/edit?usp=sharing
    NOTE: modify the snorx file if using a later version hsqldb
    modify thesmap.properties 
    modify snorx file in ctakes/resource folder 
    bash_profile is referencing the right place

FAQ:

Q: What happens if you get an error of HSQLException regarding ctakessnorx such as 
org.hsqldb.HsqlException: file input/output error: ../../../../../../../resources/org/apache/ctakes/dictionary/lookup/fast/ctakessnorx/ctakessnorx.log? 

A: This often happens after maven clean install or build happens. Replace the snorxxscripts into the resource folder at /dictionary/lookup/fast/ctakessnorx using the files from here: http://sourceforge.net/p/ctakesresources/code/HEAD/tree/trunk/ctakes-resources-snomed-rword-hsqldb-2011ab/src/main/resources/org/apache/ctakes/dictionary/lookup/fast/ctakessnorx_2_3/. This will ensure that the same version of hsqldb is used (2.0 +) rather than the default 1.8 one that is included in cTAKES.

Error Messages:
  - delete certain .m2/repository files if there is an error with not finding/reading the “archive for required file”
  - need to do mvn clean compile to make sure that you can compile ctakes
