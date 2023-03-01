## Useful Commands

### Download dependencies, compile, create artifacts

```
./mvnw package
```

### Run Docker dependencies

```shell
docker compose up
```



### Start dog-service

```shell
./mvnw -pl dog-service clean spring-boot:run
```



### Start dog-client

```shell
./mvnw -pl dog-client clean spring-boot:run
```



### Start load-generator

```shell
./mvnw -pl load-generator gatling:test
```

If you want to modify the duration of the simulation, go to `DogsSimulation.java` and edit the `duration` variable.



### Native compile

This can take a while...

```shell
./mvnw -pl dog-service -Pnative native:compile
```


## Useful links, http commands

### Backends
- Grafana: http://localhost:3000
- Prometheus: http://localhost:9090
- Prometheus targets: http://localhost:9090/targets
- Example Prometheus query: [`sum by (application) (rate(http_server_requests_seconds_count[5m]))`](http://localhost:9090/graph?g0.expr=sum%20by%20%28application%29%20%28rate%28http_server_requests_seconds_count%5B5m%5D%29%29&g0.tab=0&g0.stacked=0&g0.show_exemplars=0&g0.range_input=5m)

### Dog Service
- Dogs url: http://localhost:8080/dogs?aregood=true
- Dogs command: `http ':8080/dogs?aregood=true'`
- Get Owner's dogs url: http://localhost:8080/owner/tommy/dogs (`user:password`)
- Get Owner's dogs command: `http -a user:password :8080/owner/tommy/dogs`
- Actuator url: http://localhost:8080/actuator (`admin:secret`)
- Actuator command: `http -a admin:secret :8080/actuator`

### Dog Client
- Get Owner's dogs url: http://localhost:8081/owner/tommy/dogs
- Get Owner's dogs command: `http :8081/owner/tommy/dogs`
- Actuator url: http://localhost:8081/actuator
- Actuator command: `http :8081/actuator`

### Misc.
- Info Endpoint: http://localhost:8080/actuator/info
- Metrics Endpoint: http://localhost:8080/actuator/metrics
- Querying one metric: http://localhost:8080/actuator/metrics/http.server.requests
- Querying tags: http://localhost:8080/actuator/metrics/http.server.requests?tag=status:404
- Prometheus Endpoint: http://localhost:8080/actuator/prometheus
- Requesting OpenMetrics format: `http :8080/actuator/prometheus 'Accept: application/openmetrics-text; version=1.0.0'`



## Troubleshooting

### Maven

If you get an error that indicates a corrupt jar file (empty zip, invalid jar, etc.), e.g.:

```
java.io.IOException: Error reading file .../.m2/repository/.../something-1.2.3.jar: zip file is empty
```

You should navigate to your maven local cache and delete these files or containing folder. On Linux/Mac it should be under `"$HOME/.m2/repository"` on Windows its `%HOMEDRIVE%%HOMEPATH%\.m2\repository` (Do not remove the whole `.m2` or `.m2/repository` folder!). After removing the files, you can run `./mvnw package` again.



### Docker

If you have issues with Docker containers, you can remove them by executing

```shell
docker compose down
```

(Tempo will need ~10s to shut down.)

If you still have issues, you can try deleting the volumes too:

```shell
docker compose down --volumes
```

It can happen that Docker containers do not work because another process is occupying the same port that Docker wants to expose for a container. In this case you should find and stop the process that uses the port in question.



### IntelliJ IDEA

If IntelliJ has issues importing your project, completely close it, delete the `.idea` folder and the `*.iml` files, run `./mvnw package` and try to import the project again.



### Tempo

If everything seemingly works in Grafana, but it seems it is not able to connect to Tempo, it might be because IntelliJ IDEA (I think it opens the same port as Tempo wants - Jonatan):

1. Completely close IntelliJ
2. Restart Tempo
3. Check if Tempo works
4. Start IntelliJ again



## Manual Instrumentation

### Creating a MeterRegistry
```java
package com.example;

import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public class MicrometerDemo {
	private final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

	public static void main(String[] args) {
		new MicrometerDemo().demo();
	}

	void demo() {
		System.out.println(registry.scrape());
	}
}
```



### Micrometer: Counter + Tags
```java
void demo() {
	registry.counter(
		"test.counter",
		"application", "test"
	).increment();
	System.out.println(registry.scrape());
}
```



### Micrometer: Counter + Builder
```java
Counter.builder("test.counter")
	.description("Test counter")
	.baseUnit("events")
	.tag("application", "test")
	.register(registry) // create or get
	.increment();
```



### High Cardinality 🧐
```java
for (int i = 0; i < 100; i++) {
	Counter.builder("test.counter")
		.tag("userId", String.valueOf(i))
		.register(registry)
		.increment();
}
```



### Micrometer: Gauge
```java
public class MicrometerDemo {
	private final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
	private final AtomicLong value = new AtomicLong(); // mutable + referenced

	public static void main(String[] args) {
		new MicrometerDemo().demo();
	}

	void demo() {
		registry.gauge("test", value);
		value.set(2);
		System.out.println(registry.scrape());
	}
}
```

```java
public class MicrometerDemo {
	private final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
	private Double value = 1024.0; // immutable, don't do this :(

	public static void main(String[] args) {
		new MicrometerDemo().demo();
	}

	void demo() {
		registry.gauge("test", value);
		value = 1.0;
		System.out.println(registry.scrape());
	}
}
```

```java
public class MicrometerDemo {
	private final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
	private Double value = 1024.0; // immutable and not referenced, don't do this :(

	public static void main(String[] args) {
		new MicrometerDemo().demo();
	}

	void demo() {
		registry.gauge("test", value);
		value = 1.0;
		System.gc(); // well…
		System.out.println(registry.scrape());
	}
}
```

```java
public class MicrometerDemo {
	private final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
	private final AtomicLong value = registry.gauge("test", new AtomicLong());

	public static void main(String[] args) {
		new MicrometerDemo().demo();
	}

	void demo() {
		value.set(4);
		System.out.println(registry.scrape());
	}
}
```

```java
public class MicrometerDemo {
	private final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
	private final List<String> list = new ArrayList<>();

	public static void main(String[] args) {
		new MicrometerDemo().demo();
	}

	void demo() {
		registry.gauge("test", list, List::size);
		list.add("test");
		System.out.println(registry.scrape());
	}
}
```

```java
public class MicrometerDemo {
	private final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
	private final List<String> list = registry.gauge(
		"test",
		Tags.empty(),
		new ArrayList<>(),	// "state object"
		List::size			// "value function"
	);

	public static void main(String[] args) {
		new MicrometerDemo().demo();
	}

	void demo() {
		list.add("test");
		System.out.println(registry.scrape());
	}
}
```

```java
private final List<String> list = registry.gaugeCollectionSize(
	"test",
	Tags.empty(),
	new ArrayList<>()
);
```

```java
public class MicrometerDemo {
	private final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
	private final Map<String, Integer> map = registry.gaugeMapSize(
		"test",
		Tags.empty(),
		new HashMap<>()
	);

	public static void main(String[] args) {
		new MicrometerDemo().demo();
	}

	void demo() {
		map.put("test", 42);
		System.out.println(registry.scrape());
	}
}
```

```java
public class MicrometerDemo {
	private final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
	private final TemperatureSensor sensor = new TemperatureSensor();

	public static void main(String[] args) {
		new MicrometerDemo().demo();
	}

	void demo() {
		Gauge.builder("test", () -> sensor.getTemperature() - 273.15)
			.baseUnit("celsius")
			.register(registry);
		System.out.println(registry.scrape());
	}

	static class TemperatureSensor {
		/**
		 * @return random temperature in kelvin in the range of [283.15,303.15),
		 * if you convert it to celsius: [10,30).
		 */
		double getTemperature() {
			return Math.random() * 10 + 20 + 273.15;
		}
	}
}
```

```java
Gauge.builder("test", sensor::getTemperature)
	.baseUnit("kelvin")
	.register(registry);
```



### Micrometer: DistributionSummary
```java
public class MicrometerDemo {
	private final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

	public static void main(String[] args) {
		new MicrometerDemo().demo();
	}

	void demo() {
		DistributionSummary ds = DistributionSummary
			.builder("response.size")
			.baseUnit("bytes")
			.register(registry);

		ds.record(10);
		ds.record(20);

		System.out.println(registry.scrape());
	}
}
```



### Micrometer: Timer
```java
void demo() {
	Timer timer = Timer.builder("requests").register(registry);
	Sample sample = Timer.start();
	doSomething();
	sample.stop(timer);

	System.out.println(registry.scrape());
}

void doSomething() {
	try {
		Thread.sleep((long) (Math.random() * 100 + 100)); // [100,200) ms
	}
	catch (InterruptedException e) {
		throw new RuntimeException(e);
	}
}
```

```java
void demo() throws Exception {
	Timer timer = Timer.builder("requests").register(registry);
	timer.record(() -> doSomething());
	timer.recordCallable(() -> getSomething());

	Runnable runnable = timer.wrap(() -> doSomething());
	runnable.run();
	Callable c = timer.wrap((Callable<String>) () -> getSomething());
	c.call();
	System.out.println(registry.scrape());
}

void doSomething() {
	try {
		Thread.sleep((long) (Math.random() * 100 + 100)); // [100,200) ms
	}
	catch (InterruptedException e) {
		throw new RuntimeException(e);
	}
}

String getSomething() {
	doSomething();
	return "something";
}
```



### Micrometer: LongTaskTimer
```java
void demo() {
	LongTaskTimer ltt = LongTaskTimer.builder("test").register(registry);

	Sample sample = ltt.start();
	doSomething();
	System.out.println(registry.scrape());
	sample.stop();
}
```



### Micrometer: (Client-Side) Percentiles 🧐
```java
void demo() {
	Timer timer = Timer.builder("requests")
		.publishPercentiles(0.99, 0.999)
		.register(registry);
	Sample sample = Timer.start();
	doSomething();
	sample.stop(timer);

	System.out.println(registry.scrape());
}
```



### Micrometer: Histogram
```java
Timer timer = Timer.builder("requests")
	.publishPercentileHistogram()
	.register(registry);
```



### Micrometer: SLOs
```java
Timer timer = Timer.builder("requests")
	.serviceLevelObjectives(Duration.ofMillis(10))
	.register(registry);
```



### Micrometer: MeterProvider 🚀
```java
public class MicrometerDemo {
	private final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
	MeterProvider<Counter> provider = Counter.builder("test")
			.tag("static", "42")
			.withRegistry(registry);

	public static void main(String[] args) {
		new MicrometerDemo().demo();
	}

	void demo() {
		provider.withTags("dynamic", "123").increment();
		System.out.println(registry.scrape());
	}
}
```



### Micrometer: MeterFilter
```java
public class MicrometerDemo {
	private final PrometheusMeterRegistry registry;

	MicrometerDemo() {
		this.registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
		this.registry.config().commonTags("application", "micrometer.demo");
		this.registry.config().meterFilter(new MeterFilter() {
			@Override
			public Id map(Id id) {
				if (id.getName().equals("old"))
					return id.withName("new");
				else
					return id;
			}
		});
	}

	public static void main(String[] args) throws Exception {
		new MicrometerDemo().demo();
	}

	void demo() {
		registry.counter("old").increment();
		System.out.println(registry.scrape());
	}
}
```



### Micrometer Tracing: Span
```java
@RestController
@RequestMapping
public class DogsController {
	private final Tracer tracer;

	public DogsController(Tracer tracer) {
		this.tracer = tracer;
	}

	@GetMapping("/dogs")
	public Map<String, String> dogs(@RequestParam(name = "aregood") boolean areGood) throws InterruptedException {
		Span span = tracer.nextSpan().name("test");
		try (SpanInScope ws = tracer.withSpan(span.start())) {
			span.tag("userId", UUID.randomUUID().toString());
			Thread.sleep(1_000);
			span.event("logout");
			Thread.sleep(1_000);
		}
		finally {
			span.end();
		}

		String message = (!areGood) ? "Go find a cat service!" : "We <3 dogs!!!";
		return Map.of("message", message);
	}
}
```



### Micrometer: Observation 🚀
```java
public class MicrometerDemo {
	private final PrometheusMeterRegistry meterRegistry;
	private final ObservationRegistry observationRegistry;

	MicrometerDemo() {
		this.meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
		this.observationRegistry = ObservationRegistry.create();
		this.observationRegistry.observationConfig().observationHandler(new DefaultMeterObservationHandler(meterRegistry));
	}

	public static void main(String[] args) {
		new MicrometerDemo().demo();
	}

	void demo() {
		Observation observation = Observation.createNotStarted("talk", observationRegistry)
			.contextualName("talk observation")
			.lowCardinalityKeyValue("event", "SIO")
			.highCardinalityKeyValue("uid", UUID.randomUUID().toString());

		try (Scope scope = observation.start().openScope()) {
			doSomething();
			observation.event(Event.of("question"));
		}
		catch (Exception exception) {
			observation.error(exception);
			throw exception;
		}
		finally {
			observation.stop();
		}

		System.out.println(meterRegistry.scrape());
	}

	void doSomething() {
		try {
			Thread.sleep((long) (Math.random() * 100 + 100)); // [100,200) ms
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
```

```java
@RestController
@RequestMapping
public class DogsController {
	private final ObservationRegistry registry;

	public DogsController(ObservationRegistry registry) {
		this.registry = registry;
	}

	@GetMapping("/dogs")
	public Map<String, String> dogs(@RequestParam(name = "aregood") boolean areGood) {
		return Observation.createNotStarted("test", registry)
			.contextualName("Are dogs good?")
			.lowCardinalityKeyValue("aregood", String.valueOf(areGood))
			.highCardinalityKeyValue("uid", UUID.randomUUID().toString())
			.observe(() -> {
				String message = (!areGood) ? "Go find a cat service!" : "We <3 dogs!!!";
				return Map.of("message", message);
			});
	}
}
```

```java
registry.observationConfig()
	.observationPredicate(
		(name, ctx) -> !name.startsWith("ignored")
	);
```

```java
registry.observationConfig()
	.observationFilter(ctx -> ctx.addLowCardinalityKeyValue(KeyValue.of("abc", "42")));
```
