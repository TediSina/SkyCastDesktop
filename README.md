# SkyCast Desktop

SkyCast Desktop është një aplikacion moti në Java Swing me forma për regjistrim,
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

## Licensa

MIT License

Copyright (c) 2026 Tedi Sina, Tea Sina, Ersi Mansaku

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
