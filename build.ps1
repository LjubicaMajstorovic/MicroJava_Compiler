Remove-Item "bin" -Recurse -Force -ErrorAction SilentlyContinue
mkdir "bin" -Force
javac -cp "lib/*" -d "bin" "src/rs/ac/bg/etf/pp1/Ant.java"
java -cp "bin;lib/*" rs.ac.bg.etf.pp1.Ant compile
java -cp "bin;lib/*" rs.ac.bg.etf.pp1.Compiler