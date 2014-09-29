package eu.amidst.staticModelling.learning;


import eu.amidst.core.database.statics.DataInstance;
import eu.amidst.core.database.statics.DataStream;
import eu.amidst.core.datastructures.statics.BayesianNetwork;
import eu.amidst.core.estimator.Estimator;
import eu.amidst.core.utils.Utils;
import eu.amidst.staticModelling.models.LearnableModel;

/**
 * Created by andresmasegosa on 28/08/14.
 */
public class MaximumLikelihood implements LearningAlgorithm{

    LearnableModel model;

    @Override
    public void setLearnableModel(LearnableModel model) {
        this.model=model;
    }

    @Override
    public void initLearning() {

    }

    @Override
    public void updateModel(DataInstance data) {
        BayesianNetwork bn = model.getBayesianNetwork();
        for (int i = 0; i<bn.getNumberOfNodes(); i++){
            if (Utils.isMissing(data.getValue(i)) && bn.getVariable(i).isLeave())
                continue;

            Estimator estimator = bn.getEstimator(i);
            double[]  expPara = estimator.getExpectationParameters();
            double[]  suffStatistics = estimator.getSufficientStatistics(data);
            Utils.accumulatedSumVectors(expPara, suffStatistics);
        }
    }
    @Override
    public void learnModelFromStream(DataStream data) {
        while(data.hasMoreDataInstances()){
            this.model.updateModel(data.nextDataInstance());
        }
    }
}
