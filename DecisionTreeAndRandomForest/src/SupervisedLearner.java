// ----------------------------------------------------------------
// The contents of this file are distributed under the CC0 license.
// See http://creativecommons.org/publicdomain/zero/1.0/
// ----------------------------------------------------------------

abstract class SupervisedLearner 
{
	/// Return the name of this learner
	abstract String name();

	/// Train this supervised learner
	abstract void train(Matrix features, Matrix labels);

	/// Make a prediction
	abstract double[] predict(double[] in);

	/// Measures the misclassifications with the provided test data
	int countMisclassifications(Matrix features, Matrix labels)
	{
		if(features.rows() != labels.rows())
			throw new IllegalArgumentException("Mismatching number of rows");
		double[] pred = new double[labels.cols()];
		int mis = 0;
		for(int i = 0; i < features.rows(); i++)
		{
			double[] feat = features.row(i);
			pred = predict(feat);
			double[] lab = labels.row(i);
			for(int j = 0; j < lab.length; j++)
			{
//				System.out.println("Predict: " + pred[j]);
//				System.out.println("Lab: " + lab[j]);
				if(pred[j] != lab[j])
					mis++;
			}
		}
		return mis;
	}
}
