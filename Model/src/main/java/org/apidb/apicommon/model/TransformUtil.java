package org.apidb.apicommon.model;

import java.util.Map;
import java.util.Optional;

import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserCache;
import org.gusdb.wdk.model.user.StepContainer.ListStepContainer;

/**
 * Provides a utility to use transform questions to convert an answer value of
 * one type (record class) to another.
 * 
 * @author rdoherty
 */
public class TransformUtil {

  /**
   * Takes an answer value that returns one type and returns a new answer value
   * that uses the specified transform to produce records of a new type
   *
   * @param inputAnswer answer value that returns requiredInputType
   * @param requiredInputType full name of record class expected of inputAnswer
   * @param transformQuestionFullName full name of transform question
   * @param stepParamName name of answer param in transform question
   * @param requiredOutputType type expected to be returned by transform
   * @return transform answer value
   * @throws WdkModelException if error occurs or model does not contain objects that meet specified requirements
   */
  public static AnswerValue transformToNewResultTypeAnswer(AnswerValue inputAnswer,
      String requiredInputType, String transformQuestionFullName,
      String stepParamName, String requiredOutputType) throws WdkModelException {

    WdkModel model = inputAnswer.getWdkModel();
    User user = inputAnswer.getUser();
    AnswerSpec inputAnswerSpec = inputAnswer.getAnswerSpec();

    Optional<Strategy> strategy = inputAnswerSpec.getStepContainer() instanceof Strategy ?
        Optional.of((Strategy)inputAnswerSpec.getStepContainer()) : Optional.empty();

    RunnableObj<Step> step = Step.builder(model, user.getUserId(), model.getStepFactory().getNewStepId())
        .setAnswerSpec(AnswerSpec.builder(inputAnswer.getAnswerSpec()))
        .setStrategyId(strategy.map(strat -> strat.getStrategyId()))
        .buildRunnable(new UserCache(user), strategy);

    AnswerValue geneAnswer = AnswerValueFactory.makeAnswer(user,
        transformToNewResultTypeAnswerSpec(model, user, step, requiredInputType,
            transformQuestionFullName, stepParamName, requiredOutputType));

    // make sure new answer uses same page size as old answer
    return geneAnswer.cloneWithNewPaging(inputAnswer.getStartIndex(), inputAnswer.getEndIndex());
  }

  /**
   * Takes a runnable step of one type and returns an answer spec of a transform
   * that will return results of the output type of the transform.
   *
   * @param wdkModel
   * @param user user 
   * @param inputStep step that returns input type
   * @param requiredInputType full name of record class expected of inputStep
   * @param transformQuestionFullName full name of transform question
   * @param stepParamName name of answer param in transform question
   * @param requiredOutputType type expected to be returned by transform
   * @return transform answer spec
   * @throws WdkModelException if error occurs or model does not contain objects that meet specified requirements
   */
  private static RunnableObj<AnswerSpec> transformToNewResultTypeAnswerSpec(
      WdkModel wdkModel, User user, RunnableObj<Step> inputStep,
      String requiredInputType, String transformQuestionFullName,
      String stepParamName, String requiredOutputType) throws WdkModelException {

    if (!inputStep.get().getAnswerSpec().getQuestion().getRecordClass().getFullName().equals(requiredInputType)) {
      throw new WdkModelException("Step to be transformed must return records of type: " + requiredInputType);
    }

    Question xformQuestion = wdkModel.getQuestionByFullName(transformQuestionFullName)
        .orElseThrow(() -> new WdkModelException("Can't find xform with name: " + transformQuestionFullName));

    if (!xformQuestion.getRecordClass().getFullName().equals(requiredOutputType)) {
      throw new WdkModelException("Specified transform must return records of type: " + requiredOutputType);
    }

    if (xformQuestion.getParamMap().size() != 1 || !xformQuestion.getParamMap().containsKey(stepParamName)) {
      throw new WdkModelException("Transform question " + transformQuestionFullName +
          " must have exactly one answer parameter named " + stepParamName);
    }

    Map<String, String> transformParams = new MapBuilder<String, String>(
        stepParamName, String.valueOf(inputStep.get().getStepId())).toMap();

    return AnswerSpec
        .builder(wdkModel)
        .setQuestionFullName(transformQuestionFullName)
        .setQueryInstanceSpec(QueryInstanceSpec.builder()
            .putAll(transformParams)
            .setAssignedWeight(10)
        )
        .buildRunnable(user, new ListStepContainer(inputStep.get()));
  }

}
