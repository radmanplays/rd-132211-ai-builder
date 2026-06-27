package com.mojang.rubydung.ml;

import com.mojang.rubydung.DebugLogger;
import com.mojang.rubydung.Entity;
import java.util.List;
import java.util.Random;

public class AIManager {
  private NeuralNetwork brain;
  
  private TrainingData trainingData;
  
  private Random random = new Random();
  
  private boolean isTrainingEnabled = true;
  
  private int trainingInterval = 100;
  
  private int ticksSinceLastTraining = 0;
  
  private static final int INPUT_SIZE = 8;
  
  private static final int OUTPUT_SIZE = 5;
  
  public AIManager() {
    this.brain = new NeuralNetwork(8, 16, 5);
    this.trainingData = new TrainingData();
    DebugLogger.ml("AI Manager initialized with neural network!");
  }
  
  private double[] getInputs(Entity entity) {
    double[] inputs = new double[8];
    inputs[0] = 1.0D;
    double distance = Math.sqrt(
        Math.pow(entity.getTargetX() - entity.x, 2.0D) + 
        Math.pow(entity.getTargetZ() - entity.z, 2.0D));
    inputs[1] = Math.min(distance / 50.0D, 1.0D);
    inputs[2] = hasBlocksAhead(entity) ? 1.0D : 0.0D;
    inputs[3] = entity.getOnGround() ? 1.0D : 0.0D;
    inputs[4] = getMoodValue(entity);
    inputs[5] = Math.min(getCreativityLevel(entity) / 10.0D, 1.0D);
    inputs[6] = isStuck(entity) ? 1.0D : 0.0D;
    inputs[7] = Math.min(entity.y / 100.0D, 1.0D);
    return inputs;
  }
  
  public int predictAction(Entity entity) {
    double[] inputs = getInputs(entity);
    double[] outputs = this.brain.predict(inputs);
    int bestAction = 0;
    double maxValue = outputs[0];
    for (int i = 1; i < outputs.length; i++) {
      if (outputs[i] > maxValue) {
        maxValue = outputs[i];
        bestAction = i;
      } 
    } 
    return bestAction;
  }
  
  public void executePredictedAction(Entity entity, int action) {
    switch (action) {
      case 0:
        entity.moveForward();
        break;
      case 1:
        entity.jump();
        break;
      case 2:
        entity.tryPlaceBlock();
        break;
      case 3:
        entity.turnLeft();
        break;
      case 4:
        entity.turnRight();
        break;
    } 
  }
  
  public void train(Entity entity, int action, double reward) {
    if (!this.isTrainingEnabled)
      return; 
    double[] inputs = getInputs(entity);
    double[] targets = new double[5];
    targets[action] = 1.0D;
    this.trainingData.addExperience(inputs, targets, reward);
    this.ticksSinceLastTraining++;
    if (this.ticksSinceLastTraining >= this.trainingInterval && this.trainingData.getExperiences().size() > 10) {
      trainNetwork();
      this.ticksSinceLastTraining = 0;
    } 
  }
  
  private void trainNetwork() {
    List<TrainingData.Experience> experiences = this.trainingData.getExperiences();
    if (experiences.size() < 5)
      return; 
    int batchSize = Math.min(10, experiences.size());
    int startIndex = experiences.size() - batchSize;
    for (int i = 0; i < 10; i++) {
      for (int j = startIndex; j < experiences.size(); j++) {
        TrainingData.Experience exp = experiences.get(j);
        this.brain.train(exp.inputs, exp.outputs);
      } 
    } 
    DebugLogger.ml("Trained neural network with " + experiences.size() + " examples");
  }
  
  private boolean hasBlocksAhead(Entity entity) {
    return false;
  }
  
  private double getMoodValue(Entity entity) {
    return 0.5D;
  }
  
  private int getCreativityLevel(Entity entity) {
    return 1;
  }
  
  private boolean isStuck(Entity entity) {
    return false;
  }
  
  public void saveModel() {
    this.brain.save("ai_brain.dat");
    this.trainingData.save("ai_training.dat");
  }
  
  public void loadModel() {
    this.brain.load("ai_brain.dat");
    this.trainingData.load("ai_training.dat");
  }
  
  public void setTrainingEnabled(boolean enabled) {
    this.isTrainingEnabled = enabled;
    DebugLogger.ml("AI training " + (enabled ? "enabled" : "disabled"));
  }
  
  public String getTrainingStats() {
    return "Experiences: " + this.trainingData.getExperiences().size() + ", Training: " + (this.isTrainingEnabled ? "enabled" : "disabled");
  }
}
