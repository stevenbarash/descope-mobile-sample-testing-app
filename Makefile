.PHONY: server-install server-dev server-test server-start ios-setup ios-open ios-build ios-test android-build android-build-release android-test android-test-connected android-clean android-open android-install android-lint

# Server targets
server-install:
	cd server && npm install

server-dev:
	cd server && npm run dev

server-test:
	cd server && npm test

server-start:
	cd server && npm start

# iOS targets
ios-setup:
	cd ios && xcodegen generate

ios-open:
	cd ios && xcodegen generate && open App.xcodeproj

ios-build:
	cd ios && xcodegen generate && xcodebuild -scheme App -destination 'platform=iOS Simulator,name=iPhone 16,OS=18.4' build

ios-test:
	cd ios && xcodegen generate && xcodebuild test -scheme App -destination 'platform=iOS Simulator,name=iPhone 16,OS=18.4'

# Android targets
android-build:
	cd android && ./gradlew assembleDebug

android-build-release:
	cd android && ./gradlew assembleRelease

android-test:
	cd android && ./gradlew test

android-test-connected:
	cd android && ./gradlew connectedAndroidTest

android-clean:
	cd android && ./gradlew clean

android-open:
	open -a "Android Studio" android

android-install:
	cd android && ./gradlew installDebug

android-lint:
	cd android && ./gradlew lint
