APK := app/build/outputs/apk/debug/app-debug.apk
SRC := $(shell find app/src/main -name *.java)

default: run

.PHONY: run

run: $(APK)
	adb shell am start yap.memtest

$(APK): $(SRC)
	./gradlew assembleDebug
	adb install $(APK)

.PHONY: clean

clean:
	./gradlew clean
