# Run me from parent directory! (make -f spn2java/makefile)

JAVAC = javac
CLASSPATH = -cp .:dist/elmGen-0.5/elmGen.jar:spn2java/src
sources = $(wildcard spn2java/src/*.java)
classes = $(sources:.java=.class)

all: $(classes)

run:
	java -cp .:dist/elmGen-0.5/elmGen.jar:build SampleSimulation ROMPitch

%.class : %.java
	$(JAVAC) -d build $(CLASSPATH) $<

