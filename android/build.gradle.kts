      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v5
        with:
          gradle-version: '8.9'

      - name: Build debug APK
        run: gradle -p android assembleDebug --stacktrace
