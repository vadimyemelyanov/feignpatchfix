
# Feign Patch

Its a simple project that shows how to overcome Feign HTTP PATCH problem.


## Stackoverflow links

[Link](https://stackoverflow.com/questions/77241125/cannot-change-feign-default-client-patch-mapping-does-not-work/77416928#77416928)


## Usage/Examples
I used OkHttp client as a core
```maven
  <dependency>
    <groupId>com.squareup.okhttp</groupId>
    <artifactId>okhttp</artifactId>
    <version>2.7.5</version>
    </dependency>
    <dependency>
        <groupId>com.squareup.okhttp3</groupId>
        <artifactId>okhttp</artifactId>
        <version>4.11.0</version>
    </dependency>
    <dependency>
        <groupId>com.squareup.okio</groupId>
        <artifactId>okio</artifactId>
        <version>2.9.0</version>
    </dependency>
```

Code and configurations can be found under

`code.example.feignpatch.config` folder.



