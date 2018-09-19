# retrofit2-queue
The library supports retrofit2 request execution sequentially in Kotlin.
Java version is <a href="https://github.com/hieupham1993/retrofit2-queue">here</a>
## Installation
************* **Using Gradle** *************

Add repository:
```groovy
repositories {
    jcenter()
}
```
or
```groovy
repositories {
    maven {
        url "https://dl.bintray.com/hieupham1993/utilities" 
    }
}
```
Add this in your app's build.gradle file:

**For Gradle < 3.4**

```groovy
compile 'com.hieupt:retrofit2-queue-kotlin:1.0.2'
```

**For Gradle >= 3.4**

```groovy
implementation 'com.hieupt:retrofit2-queue-kotlin:1.0.2'
```
************* **Using Maven** *************
```xml
<dependency>
  <groupId>com.hieupt</groupId>
  <artifactId>retrofit2-queue-kotlin</artifactId>
  <version>1.0.2</version>
  <type>pom</type>
</dependency>
```
## Usage
**Create RetrofitQueue instance**
```kotlin
// new instance
val retrofitQueue = RetrofitQueue()
// singleton
val retrofitQueue = RetrofitQueueSingleton.instance
```
**Set number of request can be executed in parallel**
```kotlin
retrofitQueue.updateMaxActiveRequest(Int)
```
**Add request to queue**
```kotlin
retrofitQueue.addRequest(Call, Callback)
// or
retrofitQueue.addRequestToFrontQueue(Call, Callback)
```
**Execute a request immediately**
```kotlin
retrofitQueue.requestNow(Call, Callback)
```
**Cancel requests that executed but not finished yet**
```kotlin
// cancel all executed request
retrofitQueue.cancel()
// or cancel specific request
retrofitQueue.cancel(Call)
```
**Remove waiting requests**
```kotlin
// clear pending queue
retrofitQueue.clearQueue()
// or remove specific request
retrofitQueue.removeRequest(Call)
```
