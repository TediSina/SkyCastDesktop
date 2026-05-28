# SkyCast për desktop

SkyCast është një aplikacion moti në Java Swing me forma për regjistrim,
hyrje dhe panel kryesor. Aplikacioni ruan përdoruesit dhe kërkimet e fundit
të motit në bazën lokale SQLite `data/skycast.db`, ndërsa të dhënat e motit i
merr drejtpërdrejt nga Open-Meteo.

## Si të ekzekutohet në IntelliJ IDEA

1. Hape projektin në IntelliJ IDEA.
2. Sigurohu që moduli të ketë në classpath skedarët `.jar` nga dosja `lib/`.
3. Ekzekuto `src/Main.java`.

Bibliotekat e përfshira janë:

- `sqlite-jdbc-3.53.1.0.jar`
- `slf4j-api-1.7.36.jar`
- `slf4j-nop-1.7.36.jar`

## Si të ekzekutohet nga PowerShell

```powershell
& 'C:\Program Files\Microsoft\jdk-17.0.13.11-hotspot\bin\javac.exe' -encoding UTF-8 -cp 'lib/*' -d out (Get-ChildItem src -Filter *.java | ForEach-Object { $_.FullName })
& 'C:\Program Files\Microsoft\jdk-17.0.13.11-hotspot\bin\java.exe' -cp 'out;lib/*' Main
```

## API-të

- Gjeokodimi: `https://geocoding-api.open-meteo.com/v1/search`
- Parashikimi: `https://api.open-meteo.com/v1/forecast`
