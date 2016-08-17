# batch-collector
A simple experiment in creating a collector for Java 8 streams that accumulates into arbitrary batches.

The idea was to have a collector that could turn a Stream into a Collection of Collections, where each of the sub-Collections contains a maximum of n elements (where n is the batch size).

BatchInline is an attempt to make the collector as concise as possible, at the cost of some flexibility.

BatchCollector is an attempt to make a fully-implemented collector class where each Collection (inner and outer) can be specified by passing a `Supplier<Collection>` to the constructor (e.g. `ArrayList::new`)
