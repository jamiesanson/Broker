# Broker
Broker is an Annotation Processing Library for Android Supporting Implementation of the Repository Pattern.

## What is The Repository Pattern?
The repository pattern is a pattern allowing the seperation of concerns when retrieving data. The following diagram taken from [Microsofts
Design Pattern Wiki](https://msdn.microsoft.com/en-us/library/ff649690.aspx?f=255&MSPPError=-2147217396) describes it pretty nicely.

![Diagram of Repo Patter](https://i-msdn.sec.s-msft.com/dynimg/IC340233.png)

The key aspect of this pattern is the repository. This is a construct where you ask it for some data, and the repository decides the best
place to retrieve it from. This kind of pattern is useful for modern mobile applications, as data usage over the internet is something that
drives high uninstall rates, therefore limiting the amount of times information needs to be retrieved remotely is something of importance.

## How does it work?
Broker works by generating an implementation of a Repository interface that you define. You also define how to retrieve the information both locally and remotely, then the library does the rest. Here's a basic example of a repository with a persistent field.

```kotlin
@BrokerRepo
interface CatRepository {
    
    @Persistent(
        key = "cat_list"
    )
    fun catList(): Broker<List<Cat>>
}
```
Before project compilation, Broker looks for these annotations and generates an implementation of `CatRepository`. To then use the 
repository, you need to initialise the `BrokerRepositoryManager`. This requires implementing something known as a `Fulfiller`, which is 
responsible for retrieving the data from local or remote locations given the key defined in the annotation. For example:

```kotlin
class AppFulfiller(context: Context): Fulfiller {
        override fun <T> getLocal(key: String): T {...}

        override fun <T> getRemote(key: String): T {...}

        override fun <T> putLocal(key: String, value: T) {...}

        override fun <T> putRemote(key: String, value: T) {...}

        override fun existsLocal(key: String): Boolean {...}
}
```

Then in your app class, instantiate this manager:

```kotlin
class CatApp: Application() {
    lateinit var repoManager: BrokerRepoManager
    
    override fun onCreate() {
        super.onCreate()
        // As a builder
        repoManager = BrokerRepoManager.Builder()
                                       .fulfiller(AppFulfiller(this))
                                       .build()
        // Or Kotlins Named Args
        repoManager = BrokerRepoManager(
                          fulfiller = AppFulfiller(this)
                      )
}
```

Then you're good to go. To use the repository, ask the manager for an instance of that repository, then get your data. The broker 
backing that data will know where to look based off if the field is marked as `Transient` (i.e always gets from the remote), or `Persistent` (checks local first if it exists and isn't stale), giving you an asynchronous means of getting that data. For example, Broker supports RxJava2.

```kotlin
// First get an instance of the CatRepository
val catRepo = repoManager.get(CatRepository::class.java)

// Get the list
catRepo.catList().get()
      .subscribeOn(Schedulers.io())
      .subscribe(...)
```

## How do I use it?
Broker is current under _*heavy development*_, that means if you want to use it, fork the repo and be sure to keep up to date with 
changes as I commit more.

### Where's the version number?
I plan on beginning versioning starting at `0.1.0` after I've implemented the following:
 * Some way of not having to hardcode strings as keys
 * A nicer way of getting repository instances that doesn't require reflection and hardcoded packages
 * Artifact publishing
 * Continuous integration
 
After these are implemented I'll begin proper usage of the pull request/review system, as well as tagging and publishing artifacts such that they're usable without needing to fork this repo.
 
### Plans before v1
Following up on the previous paragraph, the _tentative_ roadmap for Broker is:
##### Release 0.2.0:
  * Have different fulfillers doing different things, i.e a remote fulfiller, a local fulfiller, maybe a cache fulfiller etc.
##### Release 0.3.0:
  * Tidy up dependencies and implement time without the use of Joda
##### Release 0.4.0:
  * Add plugin to configure how Broker builds and processes
##### Release 0.5.0:
  * I'm sure something else will come up
