.PHONY:
all: run

.PHONY:
run: compile ./src/**/*.java
	java -cp ./out/ yggdrasil.Yggdrasil

.PHONY:
compile: ./src/**/*.java
	rm -rf ./out
	mkdir ./out
	javac -d ./out/ -classpath ./out/ -sourcepath ./src/ ./src/**/*.java

.PHONY:
clean:
	rm -rf ./out