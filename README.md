<p><b>Libraries used:</b>
</br>https://github.com/DV8FromTheWorld/JDA</b>
</br>https://github.com/google/gson
</br>https://github.com/google/guava
</p>

<h2>Gradle:</h2>

```
apply plugin: 'java'

dependencies {
    compile 'net.dv8tion:JDA:3+'
    compile 'com.google.guava:guava:25.0-jre'
    implementation 'com.google.code.gson:gson:2.8.4'
}

repositories {
    jcenter()
}
```
