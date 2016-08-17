import java.util.*;
import java.util.stream.*;
import java.util.function.*;

public class BatchCollector<T>
	implements Collector<T, Collection<Collection<T>>, Collection<Collection<T>>>
{
	@FunctionalInterface
	private static interface CollectionSupplier
	{
		public <G> Collection<G> get();
	}

	private static final CollectionSupplier defaultCollectionSupplier = ArrayList::new;

	private final int batchSize;
	// batchSupplier -> for making new batches
	private final CollectionSupplier batchSupplier;
	// batchCollectionSupplier -> for making new collections of batches
	private final CollectionSupplier batchCollectionSupplier;


	public BatchCollector(int batchSize)
	{
		this(batchSize, BatchCollector.defaultCollectionSupplier);
	}

	public BatchCollector(int batchSize, CollectionSupplier genericSupplier)
	{
		this(batchSize, genericSupplier, genericSupplier);
	}

	public BatchCollector(int batchSize, CollectionSupplier batchSupplier, CollectionSupplier batchCollectionSupplier)
	{
		this.batchSize = batchSize;
		this.batchSupplier = batchSupplier;
		this.batchCollectionSupplier = batchCollectionSupplier;
	}


	public static <T> Collector<T, ?, Collection<Collection<T>>> asBatch(int batchSize)
	{
		return new BatchCollector<T>(batchSize);
	}

	public static <T> Collector<T, ?, Collection<Collection<T>>> asBatch(int batchSize, CollectionSupplier genericSupplier)
	{
		return new BatchCollector<T>(batchSize, genericSupplier);
	}

	public static <T> Collector<T, ?, Collection<Collection<T>>> asBatch(int batchSize, CollectionSupplier batchSupplier, CollectionSupplier batchCollectionSupplier)
	{
		return new BatchCollector<T>(batchSize, batchSupplier, batchCollectionSupplier);
	}


	// Interface implementation
	@Override
	public Supplier<Collection<Collection<T>>> supplier() {
		return batchCollectionSupplier::get;
	}

	@Override
	public BiConsumer<Collection<Collection<T>>, T> accumulator() {
		return this::addToNextBatch;
	}

	@Override
	public BinaryOperator<Collection<Collection<T>>> combiner() {
		return this::mergeBatches;
	}

	@Override
	public Function<Collection<Collection<T>>, Collection<Collection<T>>> finisher() {
		return (c) -> c;
	}

	@Override
	public Set<Collector.Characteristics> characteristics()
	{
		return Collections.<Collector.Characteristics>emptySet();
	}


	// Actual functionality
	private void addToNextBatch(Collection<Collection<T>> batches, T value)
	{
		batches.stream()
			.filter( c -> c.size() < batchSize )
			.findFirst()
			.orElseGet(addNewBatch(batches))
			.add(value);
	}

	private Supplier<Collection<T>> addNewBatch(Collection<Collection<T>> batches)
	{
		return () -> {
			Collection<T> batch = batchSupplier.get();
			batches.add(batch);
			return batch;
		};
	}

	private Collection<Collection<T>> mergeBatches(Collection<Collection<T>> targetBatches, Collection<Collection<T>> sourceBatches)
	{
		for (Collection<T> batch : sourceBatches) {
			for (T value : batch) {
				addToNextBatch(targetBatches, value);
			}
		};
		return targetBatches;
	}

	public static void main(String[] args)
	{
		System.out.println("Default:");
		IntStream.rangeClosed(1,100).boxed().collect(BatchCollector.asBatch(7)).forEach(System.out::println);
		System.out.println("HashSet:");
		IntStream.rangeClosed(1,100).boxed().collect(BatchCollector.asBatch(7, HashSet::new)).forEach(System.out::println);
	}

}
