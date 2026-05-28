# SkyCast Desktop

SkyCast is a Java Swing weather app with registration, login, and dashboard forms.
It stores users and recent weather searches in a local SQLite database at
`data/skycast.db`, and it fetches live weather from Open-Meteo.

## Run in IntelliJ IDEA

1. Open the project.
2. Make sure the module classpath includes the jars in `lib/`.
3. Run `src/Main.java`.

The included jars are:

- `sqlite-jdbc-3.53.1.0.jar`
- `slf4j-api-1.7.36.jar`
- `slf4j-nop-1.7.36.jar`

## Run from PowerShell

```powershell
& 'C:\Program Files\Microsoft\jdk-17.0.13.11-hotspot\bin\javac.exe' -cp 'lib/*' -d out (Get-ChildItem src -Filter *.java | ForEach-Object { $_.FullName })
& 'C:\Program Files\Microsoft\jdk-17.0.13.11-hotspot\bin\java.exe' -cp 'out;lib/*' Main
```

## APIs

- Geocoding: `https://geocoding-api.open-meteo.com/v1/search`
- Forecast: `https://api.open-meteo.com/v1/forecast`
