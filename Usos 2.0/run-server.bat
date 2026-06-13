@echo off
mvn exec:java "-Dexec.mainClass=pl.usos2.server.ServerLauncher" "-Dusos.demo.seed=false" "-Dusos.server.port=5555"
pause