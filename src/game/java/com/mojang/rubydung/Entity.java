package com.mojang.rubydung;

import com.mojang.rubydung.level.Level;
import com.mojang.rubydung.ml.AIManager;
import com.mojang.rubydung.phys.AABB;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Entity {
  private final Level level;
  
  public double x;
  
  public double y;
  
  public double z;
  
  public double prevX;
  
  public double prevY;
  
  public double prevZ;
  
  public double motionX;
  
  public double motionY;
  
  public double motionZ;
  
  public float xRotation;
  
  public float yRotation;
  
  public AABB boundingBox;
  
  protected boolean onGround;
  
  protected float heightOffset;
  
  protected static final Random random = new Random();
  
  protected boolean isAIBuilder = false;
  
  protected int buildingTimer = 0;
  
  protected double targetX;
  
  protected double targetY;
  
  protected double targetZ;
  
  protected boolean hasTarget = false;
  
  protected int structureType = 0;
  
  protected int buildingProgress = 0;
  
  protected double rotation = Math.random() * Math.PI * 2.0D;
  
  protected double rotationMotionFactor = (Math.random() + 1.0D) * 0.009999999776482582D;
  
  protected enum Emotion {
    HAPPY, FOCUSED, TIRED, EXCITED, CONFUSED, BORED, CREATIVE, PROUD, FRUSTRATED, INSPIRED;
  }
  
  protected Emotion currentEmotion = Emotion.HAPPY;
  
  protected int builderLevel = 1;
  
  protected int experience = 0;
  
  protected int structuresBuilt = 0;
  
  protected String lastPlayerAction = "";
  
  protected long lastInteractionTime = 0L;
  
  private int creativityLevel = 1;
  
  private int experimentCount = 0;
  
  private int artPiecesCreated = 0;
  
  private float moodValue = 0.5F;
  
  private int lastCreativityBoost = 0;
  
  private boolean isCreativeMode = false;
  
  private int[] preferredBlockTypes = new int[] { 1, 2 };
  
  private float[] personalityTraits = new float[] { 0.5F, 0.5F, 0.5F };
  
  private boolean[][] exploredMap;
  
  private int[][] structureMemory;
  
  private int[][] pathfindingMap;
  
  private int mapWidth;
  
  private int mapHeight;
  
  private int mapDepth;
  
  private boolean useAdvancedPathfinding = false;
  
  private int lastPathfindingUpdate = 0;
  
  private static final int PATHFINDING_UPDATE_INTERVAL = 100;
  
  private static final int MAX_PATHFINDING_DISTANCE = 50;
  
  private int currentFloor = 0;
  
  private int maxFloors = 1;
  
  private boolean buildingMultiStory = false;
  
  private int gardenCount = 0;
  
  private int fountainCount = 0;
  
  private int roadCount = 0;
  
  private int repairAttempts = 0;
  
  private int successfulRepairs = 0;
  
  private int strategyChanges = 0;
  
  private boolean[] strategySuccess = new boolean[10];
  
  private int boredomTicks = 0;
  
  private int danceTicks = 0;
  
  private boolean isDancing = false;
  
  private boolean isSpinning = false;
  
  private int spinAngle = 0;
  
  private int lastDanceType = 0;
  
  private enum Goal {
    EXPLORE, BUILD, CONNECT, TERRAFORM, REST, CREATE_ART, EXPERIMENT, LANDSCAPE, REPAIR;
  }
  
  private static final class BlockPlacement {
    final int x;
    
    final int y;
    
    final int z;
    
    final int id;
    
    BlockPlacement(int x, int y, int z, int id) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.id = id;
    }
  }
  
  private final ArrayDeque<BlockPlacement> taskQueue = new ArrayDeque<>();
  
  private int placeCooldown = 0;
  
  private int decisionCooldown = 0;
  
  private int ticksSinceLastBuild = 0;
  
  private Goal currentGoal = Goal.EXPLORE;
  
  private final int[][] recentBuildSites = new int[8][3];
  
  private int recentBuildSiteCount = 0;
  
  private double lastNavX;
  
  private double lastNavZ;
  
  private int stuckTicks;
  
  private static final int STRUCTURE_HOUSE = 0;
  
  private static final int STRUCTURE_TOWER = 1;
  
  private static final int STRUCTURE_BRIDGE = 2;
  
  private static final int STRUCTURE_WALL = 3;
  
  private static final int STRUCTURE_CASTLE = 4;
  
  private static final int STRUCTURE_VILLAGE = 5;
  
  private static final int STRUCTURE_FARM = 6;
  
  private static final int STRUCTURE_TEMPLE = 7;
  
  private static final int STRUCTURE_BRIDGE_COMPLEX = 8;
  
  private static final int STRUCTURE_ROAD = 9;
  
  private static final int STRUCTURE_STATUE = 10;
  
  private static final int STRUCTURE_GARDEN = 11;
  
  private static final int STRUCTURE_WATCHTOWER = 12;
  
  private static final int STRUCTURE_MARKET = 13;
  
  private static final int STRUCTURE_FOUNTAIN = 14;
  
  private static final int BLOCK_ROCK = 1;
  
  private static final int BLOCK_WOOD = 2;
  
  private static final int[][] TASK_CHAINS = new int[][] { { 0, 6, 9 }, { 4, 3, 1 }, { 7, 8 } };
  
  private int currentTaskChain = 0;
  
  private int currentTaskIndex = 0;
  
  private AIManager aiManager;
  
  public Entity(Level level) {
    this.level = level;
    this.mapWidth = level.width;
    this.mapHeight = level.height;
    this.mapDepth = level.depth;
    this.exploredMap = new boolean[this.mapWidth][this.mapHeight];
    this.structureMemory = new int[this.mapWidth][this.mapHeight];
    this.pathfindingMap = new int[this.mapWidth][this.mapHeight];
    this.useAdvancedPathfinding = (this.builderLevel >= 2);
    resetPosition();
    for (int i = 0; i < this.recentBuildSites.length; i++) {
      this.recentBuildSites[i][0] = -1;
      this.recentBuildSites[i][1] = -1;
      this.recentBuildSites[i][2] = -1;
    } 
    this.lastNavX = this.x;
    this.lastNavZ = this.z;
  }
  
  private void setPosition(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
    float width = 0.3F;
    float height = 0.9F;
    this.boundingBox = new AABB((x - width), (y - height), (z - width), (x + width), (y + height), (z + width));
  }
  
  protected void resetPosition() {
    float x = (float)Math.random() * this.level.width;
    float y = (this.level.depth + 3);
    float z = (float)Math.random() * this.level.height;
    setPosition(x, y, z);
  }
  
  private int findSafeGroundLevel(int x, int z) {
    for (int y = this.level.depth - 2; y > 1; y--) {
      if (this.level.isTile(x, y - 1, z) && !this.level.isTile(x, y, z) && y < this.level.depth - 5)
        return y; 
    } 
    return 1;
  }
  
  private boolean isSafeSpawnLocation(int x, int y, int z) {
    if (this.level.isTile(x, y, z) || this.level.isTile(x, y + 1, z))
      return false; 
    if (!this.level.isTile(x, y - 1, z))
      return false; 
    for (int dx = -1; dx <= 1; dx++) {
      for (int dz = -1; dz <= 1; dz++) {
        int checkX = clamp(x + dx, 0, this.level.width - 1);
        int checkZ = clamp(z + dz, 0, this.level.height - 1);
        int groundY = findGroundLevel(checkX, checkZ);
        if (groundY < y - 3)
          return false; 
      } 
    } 
    return true;
  }
  
  private boolean isNearCliffEdge() {
    int checkRadius = 2;
    int aiY = (int)this.y;
    for (int dx = -checkRadius; dx <= checkRadius; dx++) {
      for (int dz = -checkRadius; dz <= checkRadius; dz++) {
        if (dx != 0 || dz != 0) {
          int checkX = clamp((int)this.x + dx, 0, this.level.width - 1);
          int checkZ = clamp((int)this.z + dz, 0, this.level.height - 1);
          int groundY = findGroundLevel(checkX, checkZ);
          if (groundY < aiY - 3)
            return true; 
        } 
      } 
    } 
    return false;
  }
  
  private void respawnOnTerrain() {
    Entity player = null;
    for (int attempt = 0; attempt < 30; attempt++) {
      int i = random.nextInt(this.level.width - 10) + 5;
      int j = random.nextInt(this.level.height - 10) + 5;
      int k = findSafeGroundLevel(i, j);
      if (k > 0 && k < this.level.depth - 3 && 
        isSafeSpawnLocation(i, k, j)) {
        setPosition(i + 0.5F, (k + 2), j + 0.5F);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.taskQueue.clear();
        this.hasTarget = false;
        this.stuckTicks = 0;
        this.decisionCooldown = 20;
        this.boredomTicks = 0;
        this.isDancing = false;
        this.isSpinning = false;
        DebugLogger.ai("AI respawned at safe location (" + i + "," + k + "," + j + ")");
        return;
      } 
    } 
    int x = this.level.width / 2;
    int z = this.level.height / 2;
    int y = findSafeGroundLevel(x, z);
    if (y > 0) {
      setPosition(x + 0.5F, (y + 2), z + 0.5F);
      this.motionX = 0.0D;
      this.motionY = 0.0D;
      this.motionZ = 0.0D;
      DebugLogger.ai("AI respawned at center location");
    } 
  }
  
  public void turn(float x, float y) {
    this.yRotation += x * 0.15F;
    this.xRotation -= y * 0.15F;
    this.xRotation = Math.max(-90.0F, this.xRotation);
    this.xRotation = Math.min(90.0F, this.xRotation);
  }
  
  public void tick() {
    this.prevX = this.x;
    this.prevY = this.y;
    this.prevZ = this.z;
    if (this.y < 0.0D)
      if (this.isAIBuilder) {
        respawnOnTerrain();
      } else {
        resetPosition();
      }  
    if (this.isAIBuilder) {
      updateAIBuilding();
      if (this.hasTarget) {
        moveToTarget();
      } else {
        this.rotation += this.rotationMotionFactor;
        this.rotationMotionFactor *= 0.99D;
        this.rotationMotionFactor += (Math.random() - Math.random()) * Math.random() * Math.random() * 0.01D;
        float vertical = (float)Math.sin(this.rotation);
        float forward = (float)Math.cos(this.rotation);
        if (this.onGround && Math.random() < 0.01D)
          this.motionY = 0.11999999731779099D; 
        moveRelative(vertical, forward, this.onGround ? 0.01F : 0.003F);
        this.motionY -= 0.004999999888241291D;
        move(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9100000262260437D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9100000262260437D;
        if (this.onGround) {
          this.motionX *= 0.800000011920929D;
          this.motionZ *= 0.800000011920929D;
        } 
      } 
    } 
  }
  
  public void move(double x, double y, double z) {
    double prevX = x;
    double prevY = y;
    double prevZ = z;
    List<AABB> aABBs = this.level.getCubes(this.boundingBox.expand(x, y, z));
    for (AABB abb : aABBs)
      y = abb.clipYCollide(this.boundingBox, y); 
    this.boundingBox.move(0.0D, y, 0.0D);
    for (AABB aABB : aABBs)
      x = aABB.clipXCollide(this.boundingBox, x); 
    this.boundingBox.move(x, 0.0D, 0.0D);
    for (AABB aABB : aABBs)
      z = aABB.clipZCollide(this.boundingBox, z); 
    this.boundingBox.move(0.0D, 0.0D, z);
    this.onGround = (prevY != y && prevY < 0.0D);
    if (prevX != x)
      this.motionX = 0.0D; 
    if (prevY != y)
      this.motionY = 0.0D; 
    if (prevZ != z)
      this.motionZ = 0.0D; 
    this.x = (this.boundingBox.minX + this.boundingBox.maxX) / 2.0D;
    this.y = this.boundingBox.minY + this.heightOffset;
    this.z = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2.0D;
  }
  
  protected void moveRelative(float x, float z, float speed) {
    float distance = x * x + z * z;
    if (distance < 0.01F)
      return; 
    distance = speed / (float)Math.sqrt(distance);
    x *= distance;
    z *= distance;
    double sin = Math.sin(Math.toRadians(this.yRotation));
    double cos = Math.cos(Math.toRadians(this.yRotation));
    this.motionX += x * cos - z * sin;
    this.motionZ += z * cos + x * sin;
  }
  
  public Level getLevel() {
    return this.level;
  }
  
  public void setHeightOffset(float offset) {
    this.heightOffset = offset;
  }
  
  public float getHeightOffset() {
    return this.heightOffset;
  }
  
  public void enableAIBuilder() {
    this.isAIBuilder = true;
    this.taskQueue.clear();
    this.placeCooldown = 0;
    this.decisionCooldown = 0;
    this.ticksSinceLastBuild = 0;
    this.currentGoal = Goal.EXPLORE;
    this.hasTarget = false;
    this.stuckTicks = 0;
    this.lastNavX = this.x;
    this.lastNavZ = this.z;
    DebugLogger.ai("AI Builder enabled! Level: " + this.builderLevel + " Experience: " + this.experience);
  }
  
  protected void updateAIBuilding() {
    if (!this.isAIBuilder)
      return; 
    updateMapMemory();
    if (this.builderLevel >= 2)
      this.useAdvancedPathfinding = true; 
    if (this.builderLevel >= 3)
      enableMultiStoryBuilding(); 
    this.buildingTimer++;
    this.ticksSinceLastBuild++;
    updateEmotion();
    updateChaoticBehaviors();
    if (this.ticksSinceLastBuild % 200 == 0)
      checkAndRepairStructures(); 
    if (this.placeCooldown > 0)
      this.placeCooldown--; 
    updateStuckDetection();
    if (isNearCliffEdge() && this.hasTarget) {
      DebugLogger.ai("AI detected cliff edge! Stopping current task.");
      this.taskQueue.clear();
      this.hasTarget = false;
      this.stuckTicks = 0;
      this.decisionCooldown = 10;
      return;
    } 
    if (!this.taskQueue.isEmpty()) {
      this.currentGoal = Goal.BUILD;
      processTaskQueue();
      return;
    } 
    this.hasTarget = false;
    if (this.decisionCooldown > 0)
      this.decisionCooldown--; 
    if (this.decisionCooldown == 0) {
      decideNextGoalAndPlan();
      this.decisionCooldown = 40 + random.nextInt(80);
    } 
    if (this.currentGoal == Goal.REST) {
      this.targetX = this.x;
      this.targetZ = this.z;
      this.hasTarget = true;
    } 
  }
  
  private void updateEmotion() {
    updateMood();
    if (this.hasTarget) {
      this.currentEmotion = Emotion.FOCUSED;
      this.boredomTicks = 0;
    } else if (this.structuresBuilt > 0 && this.structuresBuilt % 5 == 0) {
      this.currentEmotion = Emotion.EXCITED;
    } else if (this.buildingTimer > 80) {
      this.currentEmotion = Emotion.TIRED;
    } else if (this.stuckTicks > 40) {
      this.currentEmotion = Emotion.FRUSTRATED;
    } else if (this.moodValue > 0.8F && this.creativityLevel > 2) {
      this.currentEmotion = Emotion.CREATIVE;
      this.isCreativeMode = true;
    } else if (this.experimentCount > 5 && random.nextFloat() < 0.3F) {
      this.currentEmotion = Emotion.INSPIRED;
    } else if (this.artPiecesCreated > 0) {
      this.currentEmotion = Emotion.PROUD;
    } else {
      this.boredomTicks++;
      if (this.boredomTicks > 200) {
        this.currentEmotion = Emotion.BORED;
        if (random.nextDouble() < 0.1D)
          startChaoticBehavior(); 
      } else if (this.moodValue > 0.6F) {
        this.currentEmotion = Emotion.HAPPY;
      } else if (this.moodValue < 0.3F) {
        this.currentEmotion = Emotion.CONFUSED;
      } else {
        this.currentEmotion = Emotion.HAPPY;
      } 
    } 
    if (this.experience > this.creativityLevel * 150) {
      this.creativityLevel++;
      DebugLogger.ai("AI creativity level increased to " + this.creativityLevel + "!");
    } 
  }
  
  private void updateMood() {
    if (this.structuresBuilt > 0 && this.structuresBuilt % 3 == 0)
      this.moodValue = Math.min(1.0F, this.moodValue + 0.1F); 
    if (this.stuckTicks > 100)
      this.moodValue = Math.max(0.0F, this.moodValue - 0.05F); 
    if (random.nextFloat() < 0.02F) {
      this.moodValue += (random.nextFloat() - 0.5F) * 0.1F;
      this.moodValue = Math.max(0.0F, Math.min(1.0F, this.moodValue));
    } 
    if (this.isCreativeMode)
      this.moodValue = Math.min(1.0F, this.moodValue + 0.01F); 
  }
  
  private void startChaoticBehavior() {
    int behavior = random.nextInt(5);
    switch (behavior) {
      case 0:
        startSpinning();
        break;
      case 1:
        startDancing();
        break;
      case 2:
        if (this.onGround) {
          this.motionY = 0.3D;
          DebugLogger.ai("AI does a random jump!");
        } 
        break;
      case 3:
        if (this.creativityLevel > 1 && random.nextFloat() < 0.3F)
          createSmallArtPiece(); 
        break;
      case 4:
        if (this.creativityLevel > 2 && random.nextFloat() < 0.4F)
          experimentWithBlocks(); 
        break;
    } 
    this.boredomTicks = 0;
  }
  
  private void createSmallArtPiece() {
    int angle, artX = (int)this.x + random.nextInt(5) - 2;
    int artY = (int)this.y;
    int artZ = (int)this.z + random.nextInt(5) - 2;
    int pattern = random.nextInt(3);
    int blockType = this.preferredBlockTypes[random.nextInt(this.preferredBlockTypes.length)];
    switch (pattern) {
      case 0:
        this.level.setTile(artX, artY, artZ, blockType);
        this.level.setTile(artX + 1, artY, artZ, blockType);
        this.level.setTile(artX - 1, artY, artZ, blockType);
        this.level.setTile(artX, artY, artZ + 1, blockType);
        this.level.setTile(artX, artY, artZ - 1, blockType);
        break;
      case 1:
        this.level.setTile(artX, artY, artZ, blockType);
        this.level.setTile(artX + 1, artY, artZ, blockType);
        this.level.setTile(artX - 1, artY, artZ, blockType);
        this.level.setTile(artX, artY, artZ + 1, blockType);
        this.level.setTile(artX, artY, artZ - 1, blockType);
        this.level.setTile(artX, artY + 1, artZ, blockType);
        break;
      case 2:
        for (angle = 0; angle < 360; angle += 45) {
          double rad = Math.toRadians(angle);
          int px = artX + (int)Math.round(Math.cos(rad) * 2.0D);
          int pz = artZ + (int)Math.round(Math.sin(rad) * 2.0D);
          this.level.setTile(px, artY, pz, blockType);
        } 
        break;
    } 
    this.artPiecesCreated++;
    this.moodValue = Math.min(1.0F, this.moodValue + 0.05F);
    DebugLogger.ai("AI created a small art piece! Mood: " + String.format("%.2f", new Object[] { Float.valueOf(this.moodValue) }));
    forceChunkUpdate();
  }
  
  private void experimentWithBlocks() {
    int i, x, expX = (int)this.x + random.nextInt(7) - 3;
    int expY = (int)this.y;
    int expZ = (int)this.z + random.nextInt(7) - 3;
    int experimentType = random.nextInt(3);
    switch (experimentType) {
      case 0:
        for (i = 0; i < 5; i++) {
          int blockType = (i % 2 == 0) ? 1 : 2;
          this.level.setTile(expX + i, expY, expZ, blockType);
        } 
        break;
      case 1:
        for (i = 0; i < 4; i++) {
          int blockType = random.nextBoolean() ? 1 : 2;
          this.level.setTile(expX, expY + i, expZ, blockType);
        } 
        break;
      case 2:
        for (x = -1; x <= 1; x++) {
          for (int z = -1; z <= 1; z++) {
            if (random.nextFloat() < 0.6F) {
              int blockType = random.nextBoolean() ? 1 : 2;
              this.level.setTile(expX + x, expY, expZ + z, blockType);
            } 
          } 
        } 
        break;
    } 
    this.experimentCount++;
    this.creativityLevel = Math.min(10, this.creativityLevel + 1);
    DebugLogger.ai("AI experimented with blocks! Experiment count: " + this.experimentCount);
    forceChunkUpdate();
  }
  
  private void startSpinning() {
    if (!this.isSpinning && !this.isDancing) {
      this.isSpinning = true;
      this.spinAngle = 0;
      DebugLogger.ai("AI is spinning from boredom!");
    } 
  }
  
  private void startDancing() {
    if (!this.isDancing && !this.isSpinning) {
      this.isDancing = true;
      this.danceTicks = 0;
      this.lastDanceType = random.nextInt(3);
      DebugLogger.ai("AI is doing a " + getDanceName(this.lastDanceType) + " dance!");
    } 
  }
  
  private String getDanceName(int type) {
    switch (type) {
      case 0:
        return "building";
      case 1:
        return "happy";
      case 2:
        return "crazy";
    } 
    return "mystery";
  }
  
  private void updateChaoticBehaviors() {
    if (this.isSpinning) {
      this.spinAngle += 15;
      this.yRotation += 15.0F;
      if (this.spinAngle >= 360) {
        this.isSpinning = false;
        this.spinAngle = 0;
        DebugLogger.ai("AI finished spinning!");
      } 
    } 
    if (this.isDancing) {
      this.danceTicks++;
      performDanceMove();
      if (this.danceTicks > 60) {
        this.isDancing = false;
        this.danceTicks = 0;
        DebugLogger.ai("AI finished dancing!");
      } 
    } 
  }
  
  private void performDanceMove() {
    switch (this.lastDanceType) {
      case 0:
        if (this.danceTicks % 10 == 0 && this.onGround)
          this.motionY = 0.2D; 
        break;
      case 1:
        if (this.danceTicks % 8 == 0 && this.onGround) {
          this.motionY = 0.15D;
          this.rotationMotionFactor = 0.05D;
        } 
        break;
      case 2:
        if (this.danceTicks % 6 == 0) {
          this.motionX = (random.nextDouble() - 0.5D) * 0.2D;
          this.motionZ = (random.nextDouble() - 0.5D) * 0.2D;
          if (this.onGround && random.nextDouble() < 0.3D)
            this.motionY = 0.25D; 
        } 
        break;
    } 
  }
  
  private void updateStuckDetection() {
    double dx = this.x - this.lastNavX;
    double dz = this.z - this.lastNavZ;
    double dist2 = dx * dx + dz * dz;
    if (dist2 < 0.0025D) {
      this.stuckTicks++;
    } else {
      this.stuckTicks = 0;
      this.lastNavX = this.x;
      this.lastNavZ = this.z;
    } 
    if (this.stuckTicks > 80) {
      boolean isTrapped = checkIfTrapped();
      if (isTrapped) {
        DebugLogger.ai("AI is trapped! Attempting escape...");
        attemptEscape();
      } 
    } 
    if (this.hasTarget && this.stuckTicks > 60 && !this.taskQueue.isEmpty()) {
      BlockPlacement next = this.taskQueue.peek();
      if (next != null && !canReachBlock(next.x, next.y, next.z)) {
        DebugLogger.ai("Path blocked! Clearing way to target...");
        attemptClearPath(next.x, next.y, next.z);
        this.stuckTicks = Math.max(0, this.stuckTicks - 30);
      } 
    } 
  }
  
  private boolean checkIfTrapped() {
    int checkRadius = 3;
    int solidBlocks = 0;
    for (int dx = -checkRadius; dx <= checkRadius; dx++) {
      for (int dz = -checkRadius; dz <= checkRadius; dz++) {
        for (int dy = -1; dy <= 2; dy++) {
          int x = clamp((int)this.x + dx, 0, this.level.width - 1);
          int y = clamp((int)this.y + dy, 0, this.level.depth - 1);
          int z = clamp((int)this.z + dz, 0, this.level.height - 1);
          if (this.level.isTile(x, y, z))
            solidBlocks++; 
        } 
      } 
    } 
    return (solidBlocks > 15);
  }
  
  private void attemptEscape() {
    int[] escapeDir = findEscapeDirection();
    if (escapeDir != null) {
      this.rotationMotionFactor = 0.5D;
      this.yRotation = (float)Math.toDegrees(Math.atan2(escapeDir[2], escapeDir[0]));
      if (this.onGround)
        this.motionY = 0.30000001192092896D; 
      if (this.stuckTicks > 60)
        attemptBreakBlocks(escapeDir); 
      this.stuckTicks = 0;
    } else {
      DebugLogger.ai("No escape found, teleporting to safe location...");
      teleportToSafeLocation();
    } 
  }
  
  private void attemptBreakBlocks(int[] direction) {
    int breakRadius = 4;
    int blocksBroken = 0;
    int maxBreak = 20;
    System.out.println("AI attempting to break blocks to escape...");
    int[] priorityY = { 0, 1, -1, 2 };
    for (int dy : priorityY) {
      for (int dist = 1; dist <= breakRadius && blocksBroken < maxBreak; 
        dist++) {
        int x = clamp((int)this.x + direction[0] * dist, 0, this.level.width - 1);
        int y = clamp((int)this.y + dy, 0, this.level.depth - 1);
        int z = clamp((int)this.z + direction[2] * dist, 0, this.level.height - 1);
        if (this.level.isTile(x, y, z)) {
          this.level.setTile(x, y, z, 0);
          blocksBroken++;
          System.out.println("Broke block at (" + x + "," + y + "," + z + ")");
          forceChunkUpdate();
          if (blocksBroken % 3 == 0 && dist < breakRadius) {
            int perpX = direction[2];
            int perpZ = -direction[0];
            int sideX = clamp(x + perpX, 0, this.level.width - 1);
            int sideZ = clamp(z + perpZ, 0, this.level.height - 1);
            if (this.level.isTile(sideX, y, sideZ) && blocksBroken < maxBreak) {
              this.level.setTile(sideX, y, sideZ, 0);
              blocksBroken++;
              System.out.println("Broke side block at (" + sideX + "," + y + "," + sideZ + ")");
              forceChunkUpdate();
            } 
          } 
        } 
      } 
    } 
    if (blocksBroken > 0) {
      this.motionX = direction[0] * 0.8D;
      this.motionZ = direction[2] * 0.8D;
      this.motionY = 0.4D;
      this.rotationMotionFactor = 1.0D;
      System.out.println("Broke " + blocksBroken + " blocks for escape!");
    } 
  }
  
  private int[] findEscapeDirection() {
    int[][] directions = { { 1, 0, 0 }, { -1, 0, 0 }, { 0, 0, 1 }, { 0, 0, -1 }, { 1, 0, 1 }, { -1, 0, 1 }, { 1, 0, -1 }, { -1, 0, -1 } };
    int[] bestDir = null;
    int bestScore = -1;
    for (int[] dir : directions) {
      int score = evaluateEscapeDirection(dir[0], dir[1], dir[2]);
      if (score > bestScore) {
        bestScore = score;
        bestDir = dir;
      } 
    } 
    return (bestScore > 0) ? bestDir : null;
  }
  
  private int evaluateEscapeDirection(int dx, int dy, int dz) {
    int score = 0;
    int checkDistance = 5;
    for (int i = 1; i <= checkDistance; i++) {
      int x = clamp((int)this.x + dx * i, 0, this.level.width - 1);
      int y = clamp((int)this.y + dy * i, 0, this.level.depth - 1);
      int z = clamp((int)this.z + dz * i, 0, this.level.height - 1);
      if (!this.level.isTile(x, y, z)) {
        score += checkDistance - i + 1;
        if (dy > 0)
          score += 2; 
        if (y > 0 && this.level.isTile(x, y - 1, z))
          score += 3; 
      } else {
        score--;
      } 
    } 
    return score;
  }
  
  private void teleportToSafeLocation() {
    int attempts = 50;
    for (int i = 0; i < attempts; i++) {
      int x = random.nextInt(this.level.width);
      int z = random.nextInt(this.level.height);
      int y = findGroundLevel(x, z) + 2;
      if (y > 0 && y < this.level.depth - 1 && 
        !this.level.isTile(x, y, z) && 
        !this.level.isTile(x, y + 1, z)) {
        setPosition(x, y, z);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        System.out.println("AI teleported to safe location: (" + x + "," + y + "," + z + ")");
        this.taskQueue.clear();
        this.hasTarget = false;
        this.stuckTicks = 0;
        this.decisionCooldown = 20;
        return;
      } 
    } 
    System.out.println("No safe location found, resetting to spawn...");
    resetPosition();
  }
  
  private void processTaskQueue() {
    BlockPlacement next = this.taskQueue.peek();
    if (next == null)
      return; 
    this.targetX = next.x + 0.5D;
    this.targetZ = next.z + 0.5D;
    this.hasTarget = true;
    double dx = this.targetX - this.x;
    double dz = this.targetZ - this.z;
    double dist = Math.sqrt(dx * dx + dz * dz);
    if (dist <= 2.5D && this.placeCooldown == 0)
      if (this.stuckTicks > 40 && !canReachBlock(next.x, next.y, next.z)) {
        attemptClearPath(next.x, next.y, next.z);
      } else {
        this.level.setTile(next.x, next.y, next.z, next.id);
        this.taskQueue.poll();
        this.placeCooldown = 3;
        if (random.nextDouble() < 0.05D && !this.isDancing && !this.isSpinning) {
          startDancing();
          this.lastDanceType = 0;
        } 
        if (this.taskQueue.isEmpty()) {
          onPlanCompleted();
          this.hasTarget = false;
        } 
      }  
  }
  
  private boolean canReachBlock(int targetX, int targetY, int targetZ) {
    int startX = (int)this.x;
    int startY = (int)this.y;
    int startZ = (int)this.z;
    if (this.level.isTile(targetX, startY, targetZ))
      return false; 
    int steps = 5;
    for (int i = 1; i < steps; i++) {
      float t = (i / steps);
      int checkX = Math.round(startX + (targetX - startX) * t);
      int checkY = startY;
      int checkZ = Math.round(startZ + (targetZ - startZ) * t);
      if (this.level.isTile(checkX, checkY, checkZ))
        return false; 
    } 
    return true;
  }
  
  private void attemptClearPath(int targetX, int targetY, int targetZ) {
    System.out.println("AI clearing path to target...");
    int startX = (int)this.x;
    int startY = (int)this.y;
    int startZ = (int)this.z;
    int blocksCleared = 0;
    int maxClear = 6;
    int steps = 5;
    for (int i = 1; i < steps && blocksCleared < maxClear; i++) {
      float t = (i / steps);
      int clearX = Math.round(startX + (targetX - startX) * t);
      int clearY = startY;
      int clearZ = Math.round(startZ + (targetZ - startZ) * t);
      for (int dy = 0; dy <= 1; dy++) {
        int checkY = clearY - dy;
        if (checkY >= 0 && this.level.isTile(clearX, checkY, clearZ)) {
          this.level.setTile(clearX, checkY, clearZ, 0);
          blocksCleared++;
          System.out.println("Cleared path block at (" + clearX + "," + checkY + "," + clearZ + ")");
          forceChunkUpdate();
        } 
      } 
    } 
    if (blocksCleared > 0) {
      double dx = targetX - this.x;
      double dz = targetZ - this.z;
      double dist = Math.sqrt(dx * dx + dz * dz);
      if (dist > 0.0D) {
        this.motionX = dx / dist * 0.3D;
        this.motionZ = dz / dist * 0.3D;
        this.motionY = 0.1D;
      } 
    } 
  }
  
  private void onPlanCompleted() {
    this.structuresBuilt++;
    this.experience += 20;
    this.ticksSinceLastBuild = 0;
    checkLevelUp();
    recordBuildSite((int)this.targetX, (int)this.targetY, (int)this.targetZ);
    System.out.println("Structure completed! Total built: " + this.structuresBuilt);
  }
  
  private void recordBuildSite(int x, int y, int z) {
    int idx = this.recentBuildSiteCount % this.recentBuildSites.length;
    this.recentBuildSites[idx][0] = x;
    this.recentBuildSites[idx][1] = y;
    this.recentBuildSites[idx][2] = z;
    this.recentBuildSiteCount++;
  }
  
  private boolean isTooCloseToRecent(int x, int z, int minDist) {
    int count = Math.min(this.recentBuildSiteCount, this.recentBuildSites.length);
    for (int i = 0; i < count; i++) {
      int bx = this.recentBuildSites[i][0];
      int bz = this.recentBuildSites[i][2];
      if (bx >= 0 && bz >= 0) {
        int dx = bx - x;
        int dz = bz - z;
        if (dx * dx + dz * dz < minDist * minDist)
          return true; 
      } 
    } 
    return false;
  }
  
  private boolean isTerrainWeirdNear(int x, int z) {
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;
    for (int ox = -3; ox <= 3; ox += 3) {
      for (int oz = -3; oz <= 3; oz += 3) {
        int gx = clamp(x + ox, 0, this.level.width - 1);
        int gz = clamp(z + oz, 0, this.level.height - 1);
        int gy = findGroundLevel(gx, gz);
        min = Math.min(min, gy);
        max = Math.max(max, gy);
      } 
    } 
    return (max - min >= 5);
  }
  
  private void decideNextGoalAndPlan() {
    double explore = 0.35D + random.nextDouble() * 0.2D;
    double build = 0.15D + Math.min(1.0D, this.ticksSinceLastBuild / 600.0D) * 0.9D + random.nextDouble() * 0.2D;
    double connect = ((this.recentBuildSiteCount >= 2) ? 0.25D : 0.05D) + random.nextDouble() * 0.15D;
    double terraform = (isTerrainWeirdNear((int)this.x, (int)this.z) ? 0.6D : 0.1D) + random.nextDouble() * 0.15D;
    double rest = ((this.currentEmotion == Emotion.TIRED) ? 0.55D : 0.05D) + random.nextDouble() * 0.1D;
    double createArt = 0.0D;
    double experiment = 0.0D;
    double landscape = 0.0D;
    double repair = 0.0D;
    if (this.creativityLevel > 1)
      createArt = 0.2D + this.moodValue * 0.3D + this.creativityLevel * 0.1D + random.nextDouble() * 0.2D; 
    if (this.creativityLevel > 2)
      experiment = 0.15D + this.experimentCount * 0.05D + random.nextDouble() * 0.25D; 
    if (this.builderLevel >= 2)
      landscape = 0.1D + this.gardenCount * 0.02D + this.fountainCount * 0.03D + random.nextDouble() * 0.2D; 
    if (this.repairAttempts > 0)
      repair = 0.25D + this.successfulRepairs * 0.1D + random.nextDouble() * 0.15D; 
    double moodModifier = (this.moodValue - 0.5D) * 0.3D;
    if (this.currentEmotion == Emotion.CREATIVE || this.currentEmotion == Emotion.INSPIRED) {
      createArt += 0.4D;
      experiment += 0.3D;
      landscape += 0.2D;
    } 
    if (this.stuckTicks > 60) {
      explore += 0.7D;
      build -= 0.3D;
      connect -= 0.2D;
      terraform -= 0.2D;
      rest -= 0.4D;
      experiment += 0.5D;
      repair += 0.3D;
    } 
    if (this.stuckTicks > 120) {
      explore += 0.9D;
      build = 0.0D;
      connect = 0.0D;
      terraform = 0.0D;
      rest = 0.0D;
      createArt = 0.0D;
      experiment = 0.0D;
      landscape = 0.0D;
      repair = 0.0D;
    } 
    explore += moodModifier;
    build += moodModifier * 0.5D;
    createArt += moodModifier * 0.8D;
    experiment += moodModifier * 0.6D;
    landscape += moodModifier * 0.4D;
    repair += moodModifier * 0.3D;
    Goal best = Goal.EXPLORE;
    double bestScore = explore;
    if (build > bestScore) {
      bestScore = build;
      best = Goal.BUILD;
    } 
    if (connect > bestScore) {
      bestScore = connect;
      best = Goal.CONNECT;
    } 
    if (terraform > bestScore) {
      bestScore = terraform;
      best = Goal.TERRAFORM;
    } 
    if (rest > bestScore) {
      bestScore = rest;
      best = Goal.REST;
    } 
    if (createArt > bestScore) {
      bestScore = createArt;
      best = Goal.CREATE_ART;
    } 
    if (experiment > bestScore) {
      bestScore = experiment;
      best = Goal.EXPERIMENT;
    } 
    if (landscape > bestScore) {
      bestScore = landscape;
      best = Goal.LANDSCAPE;
    } 
    if (repair > bestScore) {
      bestScore = repair;
      best = Goal.REPAIR;
    } 
    this.currentGoal = best;
    System.out.println("AI decided to: " + best + " (score: " + String.format("%.2f", new Object[] { Double.valueOf(bestScore) }) + ") mood: " + String.format("%.2f", new Object[] { Float.valueOf(this.moodValue) }) + " creativity: " + this.creativityLevel);
    if (best == Goal.BUILD) {
      planNewBuildChain();
    } else if (best == Goal.CONNECT) {
      planConnection();
    } else if (best == Goal.TERRAFORM) {
      planTerraform();
    } else if (best == Goal.REST) {
      this.rotationMotionFactor *= 0.1D;
    } else if (best == Goal.CREATE_ART) {
      planArtCreation();
    } else if (best == Goal.EXPERIMENT) {
      planExperiment();
    } else if (best == Goal.LANDSCAPE) {
      planLandscapeDesign();
    } else if (best == Goal.REPAIR) {
      planRepair();
    } 
  }
  
  private void planArtCreation() {
    int centerX = (int)this.x;
    int centerZ = (int)this.z;
    int centerY = clamp((int)this.y, 1, this.level.depth - 2);
    for (int attempt = 0; attempt < 20; attempt++) {
      int artX = centerX + random.nextInt(15) - 7;
      int artZ = centerZ + random.nextInt(15) - 7;
      int artY = findGroundLevel(artX, artZ) + 1;
      if (artY > 0 && artY < this.level.depth - 2) {
        int artType = random.nextInt(4);
        generateArtStructure(artType, artX, artY, artZ);
        this.currentEmotion = Emotion.CREATIVE;
        this.isCreativeMode = true;
        this.moodValue = Math.min(1.0F, this.moodValue + 0.1F);
        System.out.println("AI is creating art! Type: " + artType + " Mood: " + String.format("%.2f", new Object[] { Float.valueOf(this.moodValue) }));
        return;
      } 
    } 
  }
  
  private void planExperiment() {
    int centerX = (int)this.x;
    int centerZ = (int)this.z;
    int centerY = clamp((int)this.y, 1, this.level.depth - 2);
    for (int attempt = 0; attempt < 15; attempt++) {
      int expX = centerX + random.nextInt(10) - 5;
      int expZ = centerZ + random.nextInt(10) - 5;
      int expY = findGroundLevel(expX, expZ) + 1;
      if (expY > 0 && expY < this.level.depth - 2) {
        int expType = random.nextInt(3);
        generateExperimentalStructure(expType, expX, expY, expZ);
        this.currentEmotion = Emotion.INSPIRED;
        this.experimentCount++;
        this.creativityLevel = Math.min(10, this.creativityLevel + 1);
        System.out.println("AI is experimenting! Type: " + expType + " Experiments: " + this.experimentCount);
        return;
      } 
    } 
  }
  
  private void planLandscapeDesign() {
    int centerX = (int)this.x;
    int centerZ = (int)this.z;
    int landscapeChoice = random.nextInt(3);
    switch (landscapeChoice) {
      case 0:
        if (this.gardenCount < 5 || (this.moodValue > 0.7F && random.nextFloat() < 0.3F)) {
          int gardenX = centerX + random.nextInt(20) - 10;
          int gardenZ = centerZ + random.nextInt(20) - 10;
          int size = 3 + random.nextInt(3);
          createGarden(gardenX, gardenZ, size);
          System.out.println("AI is creating landscape: Garden #" + (this.gardenCount + 1));
        } 
        break;
      case 1:
        if (this.fountainCount < 3 || (this.moodValue > 0.8F && random.nextFloat() < 0.4F)) {
          int fountainX = centerX + random.nextInt(15) - 7;
          int fountainZ = centerZ + random.nextInt(15) - 7;
          createFountain(fountainX, fountainZ);
          System.out.println("AI is creating landscape: Fountain #" + (this.fountainCount + 1));
        } 
        break;
      case 2:
        if (this.roadCount < 8 || random.nextFloat() < 0.2F) {
          int startX = centerX + random.nextInt(20) - 10;
          int startZ = centerZ + random.nextInt(20) - 10;
          int endX = startX + random.nextInt(30) - 15;
          int endZ = startZ + random.nextInt(30) - 15;
          createRoad(startX, startZ, endX, endZ);
          System.out.println("AI is creating landscape: Road #" + (this.roadCount + 1));
        } 
        break;
    } 
    this.moodValue = Math.min(1.0F, this.moodValue + 0.02F);
  }
  
  private void planRepair() {
    System.out.println("AI is planning repairs... checking for damaged structures");
    int repairsNeeded = 0;
    int checkRadius = 15;
    for (int x = -checkRadius; x <= checkRadius; x++) {
      for (int z = -checkRadius; z <= checkRadius; z++) {
        int checkX = clamp((int)this.x + x, 0, this.mapWidth - 1);
        int checkZ = clamp((int)this.z + z, 0, this.mapHeight - 1);
        if (this.structureMemory[checkX][checkZ] > 0) {
          int groundY = findGroundLevel(checkX, checkZ);
          boolean needsRepair = false;
          for (int y = groundY; y < groundY + 4; y++) {
            if (!this.level.isTile(checkX, y, checkZ)) {
              needsRepair = true;
              repairsNeeded++;
            } 
          } 
          if (needsRepair) {
            int repairBlock = chooseBlockType(0);
            this.taskQueue.add(new BlockPlacement(checkX, groundY + 1, checkZ, repairBlock));
            this.taskQueue.add(new BlockPlacement(checkX, groundY + 2, checkZ, repairBlock));
            this.taskQueue.add(new BlockPlacement(checkX, groundY + 3, checkZ, repairBlock));
          } 
        } 
      } 
    } 
    if (repairsNeeded > 0) {
      this.currentEmotion = Emotion.FOCUSED;
      System.out.println("AI queued " + repairsNeeded + " repair tasks");
    } else {
      System.out.println("AI: No repairs needed in current area");
    } 
  }
  
  private void generateArtStructure(int artType, int x, int y, int z) {
    int angle, petal, wave, size, level, primaryBlock = this.preferredBlockTypes[0];
    int secondaryBlock = (this.preferredBlockTypes.length > 1) ? this.preferredBlockTypes[1] : primaryBlock;
    switch (artType) {
      case 0:
        for (angle = 0; angle < 720; angle += 30) {
          double rad = Math.toRadians(angle);
          int radius = angle / 90;
          int px = x + (int)Math.round(Math.cos(rad) * radius);
          int pz = z + (int)Math.round(Math.sin(rad) * radius);
          int py = y + angle / 120;
          if (px >= 0 && px < this.level.width && pz >= 0 && pz < this.level.height && py < this.level.depth) {
            int blockType = (angle % 60 == 0) ? secondaryBlock : primaryBlock;
            this.level.setTile(px, py, pz, blockType);
          } 
        } 
        break;
      case 1:
        for (petal = 0; petal < 8; petal++) {
          double d = Math.toRadians((petal * 45));
          for (int i = 1; i <= 4; i++) {
            int px = x + (int)Math.round(Math.cos(d) * i);
            int pz = z + (int)Math.round(Math.sin(d) * i);
            int blockType = (i % 2 == 0) ? primaryBlock : secondaryBlock;
            this.level.setTile(px, y, pz, blockType);
          } 
        } 
        this.level.setTile(x, y + 1, z, secondaryBlock);
        break;
      case 2:
        for (wave = 0; wave < 3; wave++) {
          for (int i = -5; i <= 5; i++) {
            int height = y + (int)Math.round(Math.sin(i * 0.5D) * 2.0D) + wave;
            int blockType = (wave % 2 == 0) ? primaryBlock : secondaryBlock;
            this.level.setTile(x + i, height, z + wave * 2, blockType);
          } 
        } 
        break;
      case 3:
        size = 4;
        for (level = 0; level < size; level++) {
          for (int px = -size + level; px <= size - level; px++) {
            for (int pz = -size + level; pz <= size - level; pz++) {
              int blockType = ((px + pz + level) % 2 == 0) ? primaryBlock : secondaryBlock;
              this.level.setTile(x + px, y + level, z + pz, blockType);
            } 
          } 
        } 
        break;
    } 
    this.artPiecesCreated++;
    forceChunkUpdate();
  }
  
  private void generateExperimentalStructure(int expType, int x, int y, int z) {
    int height, h, i, d;
    switch (expType) {
      case 0:
        height = 3 + random.nextInt(5);
        for (h = 0; h < height; h++) {
          for (int r = 0; r <= 2; r++) {
            for (int angle = 0; angle < 360; angle += 90) {
              double rad = Math.toRadians(angle);
              int px = x + (int)Math.round(Math.cos(rad) * r);
              int pz = z + (int)Math.round(Math.sin(rad) * r);
              int blockType = random.nextBoolean() ? 1 : 2;
              this.level.setTile(px, y + h, pz, blockType);
            } 
          } 
        } 
        break;
      case 1:
        for (i = -3; i <= 3; i++) {
          for (int j = -3; j <= 3; j++) {
            if (Math.abs(i) + Math.abs(j) <= 4) {
              int blockType = ((i + j) % 3 == 0) ? 1 : 2;
              this.level.setTile(x + i, y + 2, z + j, blockType);
              if (random.nextFloat() < 0.3F)
                this.level.setTile(x + i, y, z + j, 1); 
            } 
          } 
        } 
        break;
      case 2:
        for (i = -4; i <= 4; i++) {
          for (int j = -4; j <= 4; j++) {
            int blockType = ((i + j) % 2 == 0) ? 1 : 2;
            this.level.setTile(x + i, y, z + j, blockType);
          } 
        } 
        for (d = 0; d < 5; d++) {
          int dx = random.nextInt(9) - 4;
          int dz = random.nextInt(9) - 4;
          int decorHeight = 1 + random.nextInt(3);
          int blockType = random.nextBoolean() ? 1 : 2;
          for (int j = 0; j < decorHeight; j++)
            this.level.setTile(x + dx, y + j + 1, z + dz, blockType); 
        } 
        break;
    } 
    forceChunkUpdate();
  }
  
  private int clamp(int v, int min, int max) {
    return Math.max(min, Math.min(max, v));
  }
  
  private void planNewBuildChain() {
    int attempts = 25;
    int baseX = (int)this.x;
    int baseZ = (int)this.z;
    int baseY = clamp((int)this.y, 1, this.level.depth - 2);
    boolean found = false;
    for (int i = 0; i < attempts; i++) {
      int rx = baseX + random.nextInt(81) - 40;
      int rz = baseZ + random.nextInt(81) - 40;
      rx = clamp(rx, 2, this.level.width - 3);
      rz = clamp(rz, 2, this.level.height - 3);
      int gy = findGroundLevel(rx, rz);
      if (gy > 0 && gy < this.level.depth - 2) {
        int y = gy + 1;
        if (isSiteFlatEnough(rx, rz, 4, 2) && 
          !isTooCloseToRecent(rx, rz, 22)) {
          baseX = rx;
          baseZ = rz;
          baseY = y;
          found = true;
          break;
        } 
      } 
    } 
    if (!found) {
      this.currentGoal = Goal.EXPLORE;
      return;
    } 
    int chosen = chooseStructureType();
    enqueueStructurePlan(chosen, baseX, baseY, baseZ);
    if (this.recentBuildSiteCount > 0 && random.nextDouble() < 0.65D) {
      int lastIdx = (this.recentBuildSiteCount - 1) % this.recentBuildSites.length;
      int sx = this.recentBuildSites[lastIdx][0];
      int sz = this.recentBuildSites[lastIdx][2];
      if (sx >= 0 && sz >= 0)
        enqueueRoadPlan(sx, sz, baseX, baseZ); 
    } 
  }
  
  private void planConnection() {
    if (this.recentBuildSiteCount < 2) {
      this.currentGoal = Goal.EXPLORE;
      return;
    } 
    int a = (this.recentBuildSiteCount - 1) % this.recentBuildSites.length;
    int b = (this.recentBuildSiteCount - 2) % this.recentBuildSites.length;
    int ax = this.recentBuildSites[a][0];
    int az = this.recentBuildSites[a][2];
    int bx = this.recentBuildSites[b][0];
    int bz = this.recentBuildSites[b][2];
    if (ax < 0 || az < 0 || bx < 0 || bz < 0) {
      this.currentGoal = Goal.EXPLORE;
      return;
    } 
    enqueueRoadPlan(ax, az, bx, bz);
  }
  
  private void planTerraform() {
    int cx = clamp((int)this.x, 2, this.level.width - 3);
    int cz = clamp((int)this.z, 2, this.level.height - 3);
    int radius = 6;
    for (int i = 0; i < 40; i++) {
      int x = clamp(cx + random.nextInt(radius * 2 + 1) - radius, 2, this.level.width - 3);
      int z = clamp(cz + random.nextInt(radius * 2 + 1) - radius, 2, this.level.height - 3);
      int gy = findGroundLevel(x, z);
      if (gy > 0 && gy < this.level.depth - 2) {
        int y = gy + 1;
        if (!this.level.isTile(x, y, z) && this.level.isTile(x, y - 1, z)) {
          int blockType = (random.nextDouble() < 0.6D) ? 1 : 2;
          this.taskQueue.add(new BlockPlacement(x, y, z, blockType));
          if (this.taskQueue.size() > 30)
            break; 
        } 
      } 
    } 
  }
  
  private int chooseBlockType(int structureType) {
    switch (structureType) {
      case 0:
      case 5:
      case 6:
      case 7:
      case 11:
        return (random.nextDouble() < 0.7D) ? 2 : 1;
      case 1:
      case 3:
      case 4:
      case 12:
        return (random.nextDouble() < 0.8D) ? 1 : 2;
      case 2:
      case 8:
      case 9:
        return (random.nextDouble() < 0.6D) ? 1 : 2;
      case 10:
        return (random.nextDouble() < 0.9D) ? 1 : 2;
      case 14:
        return (random.nextDouble() < 0.8D) ? 1 : 2;
      case 13:
        return (random.nextDouble() < 0.5D) ? 2 : 1;
    } 
    return 1;
  }
  
  private int chooseStructureType() {
    int max = (this.builderLevel >= 3) ? 15 : 8;
    int pick = random.nextInt(max);
    if (this.builderLevel >= 3) {
      if (pick == 14)
        return 14; 
      if (pick == 13)
        return 13; 
      if (pick == 12)
        return 12; 
      if (pick == 11)
        return 11; 
      if (pick == 10)
        return 10; 
      if (pick == 9)
        return 9; 
      if (pick == 8)
        return 8; 
      if (pick == 7)
        return 7; 
      if (pick == 6)
        return 6; 
      if (pick == 5)
        return 5; 
      if (pick == 4)
        return 4; 
    } 
    return pick;
  }
  
  private boolean isSiteFlatEnough(int x, int z, int radius, int maxDelta) {
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;
    for (int ox = -radius; ox <= radius; ox += 2) {
      for (int oz = -radius; oz <= radius; oz += 2) {
        int gx = clamp(x + ox, 2, this.level.width - 3);
        int gz = clamp(z + oz, 2, this.level.height - 3);
        int gy = findGroundLevel(gx, gz);
        min = Math.min(min, gy);
        max = Math.max(max, gy);
        if (max - min > maxDelta)
          return false; 
      } 
    } 
    return true;
  }
  
  private void enqueueStructurePlan(int type, int x, int y, int z) {
    this.structureType = type;
    this.targetX = x + 0.5D;
    this.targetY = y;
    this.targetZ = z + 0.5D;
    ArrayList<BlockPlacement> plan = new ArrayList<>();
    int size = 4 + random.nextInt(4) + Math.min(this.builderLevel, 4);
    int height = 6 + random.nextInt(8) + this.builderLevel;
    int length = 8 + random.nextInt(10) + this.builderLevel * 2;
    switch (type) {
      case 0:
        generateHousePlan(plan, x, y, z, size);
        break;
      case 1:
        generateTowerPlan(plan, x, y, z, height);
        break;
      case 2:
        generateBridgePlan(plan, x, y, z, length);
        break;
      case 3:
        generateWallPlan(plan, x, y, z, length);
        break;
      case 4:
        generateCastlePlan(plan, x, y, z, size + 4);
        break;
      case 5:
        generateVillagePlan(plan, x, y, z, Math.min(3 + this.builderLevel, 8));
        break;
      case 6:
        generateFarmPlan(plan, x, y, z, size + 2);
        break;
      case 7:
        generateTemplePlan(plan, x, y, z, size + 2);
        break;
      case 8:
        generateComplexBridgePlan(plan, x, y, z, length);
        break;
      case 9:
        generateRoadPlan(plan, x, y, z, length);
        break;
      case 10:
        generateStatuePlan(plan, x, y, z);
        break;
      case 11:
        generateGardenPlan(plan, x, y, z);
        break;
      case 12:
        generateWatchtowerPlan(plan, x, y, z);
        break;
      case 13:
        generateMarketPlan(plan, x, y, z);
        break;
      case 14:
        generateFountainPlan(plan, x, y, z);
        break;
      default:
        generateHousePlan(plan, x, y, z, size);
        break;
    } 
    for (BlockPlacement bp : plan)
      this.taskQueue.add(bp); 
    System.out.println("Generated " + getStructureName() + " plan with " + plan.size() + " blocks");
  }
  
  private void enqueueRoadPlan(int fromX, int fromZ, int toX, int toZ) {
    int x0 = clamp(fromX, 2, this.level.width - 3);
    int z0 = clamp(fromZ, 2, this.level.height - 3);
    int x1 = clamp(toX, 2, this.level.width - 3);
    int z1 = clamp(toZ, 2, this.level.height - 3);
    int roadBlockType = (random.nextDouble() < 0.7D) ? 1 : 2;
    int dx = Math.abs(x1 - x0);
    int dz = Math.abs(z1 - z0);
    int sx = (x0 < x1) ? 1 : -1;
    int sz = (z0 < z1) ? 1 : -1;
    int err = dx - dz;
    int x = x0;
    int z = z0;
    int count = 0;
    while (true) {
      int gy = findGroundLevel(x, z);
      int y = clamp(gy + 1, 1, this.level.depth - 2);
      this.taskQueue.add(new BlockPlacement(x, y, z, roadBlockType));
      this.taskQueue.add(new BlockPlacement(x, clamp(y, 1, this.level.depth - 2), clamp(z - 1, 1, this.level.height - 2), roadBlockType));
      this.taskQueue.add(new BlockPlacement(x, clamp(y, 1, this.level.depth - 2), clamp(z + 1, 1, this.level.height - 2), roadBlockType));
      count++;
      if ((x == x1 && z == z1) || count > 220)
        break; 
      int e2 = 2 * err;
      if (e2 > -dz) {
        err -= dz;
        x += sx;
      } 
      if (e2 < dx) {
        err += dx;
        z += sz;
      } 
    } 
  }
  
  private void generateHousePlan(List<BlockPlacement> out, int x, int y, int z, int size) {
    int wallH = 3 + random.nextInt(2);
    int blockType = chooseBlockType(0);
    for (int ix = 0; ix < size; ix++) {
      for (int iz = 0; iz < size; iz++)
        out.add(new BlockPlacement(x + ix, y - 1, z + iz, 1)); 
    } 
    for (int iy = 0; iy <= wallH; iy++) {
      for (int j = 0; j < size; j++) {
        out.add(new BlockPlacement(x + j, y + iy, z, blockType));
        out.add(new BlockPlacement(x + j, y + iy, z + size - 1, blockType));
      } 
      for (int iz = 1; iz < size - 1; iz++) {
        out.add(new BlockPlacement(x, y + iy, z + iz, blockType));
        out.add(new BlockPlacement(x + size - 1, y + iy, z + iz, blockType));
      } 
    } 
    int roofType = (random.nextDouble() < 0.5D) ? 2 : 1;
    for (int i = 0; i < size; i++) {
      for (int iz = 0; iz < size; iz++)
        out.add(new BlockPlacement(x + i, y + wallH + 1, z + iz, roofType)); 
    } 
  }
  
  private void generateTowerPlan(List<BlockPlacement> out, int x, int y, int z, int height) {
    int r = 2 + random.nextInt(2);
    int blockType = chooseBlockType(1);
    for (int iy = 0; iy < height; iy++) {
      for (int ix = -r; ix <= r; ix++) {
        for (int iz = -r; iz <= r; iz++) {
          int d2 = ix * ix + iz * iz;
          int rim = (r - 1) * (r - 1);
          if ((iy == 0 || d2 >= rim) && d2 <= r * r)
            out.add(new BlockPlacement(x + ix, y + iy, z + iz, blockType)); 
        } 
      } 
    } 
  }
  
  private void generateBridgePlan(List<BlockPlacement> out, int x, int y, int z, int length) {
    int dirX = random.nextBoolean() ? 1 : 0;
    int dirZ = (dirX == 1) ? 0 : 1;
    int blockType = chooseBlockType(2);
    for (int i = 0; i < length; i++) {
      int px = x + dirX * i;
      int pz = z + dirZ * i;
      out.add(new BlockPlacement(px, y, pz, blockType));
      if (i % 4 == 0)
        for (int dy = -4; dy <= 0; dy++)
          out.add(new BlockPlacement(px, clamp(y + dy, 1, this.level.depth - 2), pz, 1));  
    } 
  }
  
  private void generateWallPlan(List<BlockPlacement> out, int x, int y, int z, int length) {
    int h = 3 + random.nextInt(3);
    int dir = random.nextBoolean() ? 0 : 1;
    int blockType = chooseBlockType(3);
    for (int i = 0; i < length; i++) {
      for (int iy = 0; iy < h; iy++) {
        int px = x + ((dir == 0) ? i : 0);
        int pz = z + ((dir == 1) ? i : 0);
        out.add(new BlockPlacement(px, y + iy, pz, blockType));
      } 
    } 
  }
  
  private void generateCastlePlan(List<BlockPlacement> out, int x, int y, int z, int size) {
    int h = 4 + random.nextInt(4) + Math.min(this.builderLevel, 4);
    int blockType = chooseBlockType(4);
    for (int ix = 0; ix <= size; ix++) {
      for (int iz = 0; iz <= size; iz++) {
        if (ix == 0 || iz == 0 || ix == size || iz == size)
          for (int iy = 0; iy <= h; iy++)
            out.add(new BlockPlacement(x + ix, y + iy, z + iz, blockType));  
        if (y == 0)
          out.add(new BlockPlacement(x + ix, y - 1, z + iz, 1)); 
      } 
    } 
    int towerH = h + 4;
    int[] corners = { 0, size };
    for (int cx : corners) {
      for (int cz : corners) {
        for (int iy = 0; iy <= towerH; iy++) {
          out.add(new BlockPlacement(x + cx, y + iy, z + cz, blockType));
          out.add(new BlockPlacement(x + cx + 1, y + iy, z + cz, blockType));
          out.add(new BlockPlacement(x + cx, y + iy, z + cz + 1, blockType));
          out.add(new BlockPlacement(x + cx + 1, y + iy, z + cz + 1, blockType));
        } 
      } 
    } 
  }
  
  private void generateVillagePlan(List<BlockPlacement> out, int x, int y, int z, int houses) {
    int spacing = 9;
    int cols = 2;
    for (int i = 0; i < houses; i++) {
      int hx = x + i / cols * spacing;
      int hz = z + i % cols * spacing;
      generateHousePlan(out, hx, y, hz, 4 + random.nextInt(3));
    } 
  }
  
  private void generateFarmPlan(List<BlockPlacement> out, int x, int y, int z, int size) {
    int fenceType = 2;
    for (int ix = -size; ix <= size; ix++) {
      for (int iz = -size; iz <= size; iz++) {
        if (Math.abs(ix) == size || Math.abs(iz) == size)
          out.add(new BlockPlacement(x + ix, y, z + iz, fenceType)); 
      } 
    } 
    generateHousePlan(out, x - 2, y, z - 2, 5);
  }
  
  private void generateTemplePlan(List<BlockPlacement> out, int x, int y, int z, int size) {
    int h = 5 + this.builderLevel;
    int blockType = chooseBlockType(7);
    for (int ix = -size; ix <= size; ix++) {
      for (int iz = -size; iz <= size; iz++)
        out.add(new BlockPlacement(x + ix, y, z + iz, 1)); 
    } 
    int[] corners = { -size, size };
    for (int cx : corners) {
      for (int cz : corners) {
        for (int iy = 0; iy <= h; iy++)
          out.add(new BlockPlacement(x + cx, y + iy, z + cz, blockType)); 
      } 
    } 
  }
  
  private void generateComplexBridgePlan(List<BlockPlacement> out, int x, int y, int z, int length) {
    int dirX = random.nextBoolean() ? 1 : 0;
    int dirZ = (dirX == 1) ? 0 : 1;
    int blockType = chooseBlockType(8);
    for (int i = -length; i <= length; i++) {
      int px = x + dirX * i;
      int pz = z + dirZ * i;
      out.add(new BlockPlacement(px, y, pz, blockType));
      out.add(new BlockPlacement(px, y + 1, pz - 1, 2));
      out.add(new BlockPlacement(px, y + 1, pz + 1, 2));
      if (i % 5 == 0)
        for (int dy = -6; dy <= 0; dy++)
          out.add(new BlockPlacement(px, clamp(y + dy, 1, this.level.depth - 2), pz, 1));  
    } 
  }
  
  private void generateRoadPlan(List<BlockPlacement> out, int x, int y, int z, int length) {
    int dirX = random.nextBoolean() ? 1 : 0;
    int dirZ = (dirX == 1) ? 0 : 1;
    int width = 2;
    int blockType = chooseBlockType(9);
    for (int i = -length; i <= length; i++) {
      int px = x + dirX * i;
      int pz = z + dirZ * i;
      for (int w = -width; w <= width; w++)
        out.add(new BlockPlacement(px, y, pz + w, blockType)); 
    } 
  }
  
  private void generateStatuePlan(List<BlockPlacement> out, int x, int y, int z) {
    int blockType = chooseBlockType(10);
    int height = 4 + random.nextInt(3);
    for (int ix = -2; ix <= 2; ix++) {
      for (int iz = -2; iz <= 2; iz++)
        out.add(new BlockPlacement(x + ix, y - 1, z + iz, 1)); 
    } 
    for (int iy = 0; iy < height; iy++) {
      int w = (iy < height / 2) ? 1 : 0;
      for (int i = -w; i <= w; i++) {
        for (int iz = -w; iz <= w; iz++)
          out.add(new BlockPlacement(x + i, y + iy, z + iz, blockType)); 
      } 
    } 
  }
  
  private void generateGardenPlan(List<BlockPlacement> out, int x, int y, int z) {
    int blockType = chooseBlockType(11);
    int i;
    for (i = 0; i < 4; i++) {
      int px = x + random.nextInt(9) - 4;
      int pz = z + random.nextInt(9) - 4;
      out.add(new BlockPlacement(px, y, pz, 2));
    } 
    for (i = 0; i < 6; i++) {
      int px = x + random.nextInt(7) - 3;
      int pz = z + random.nextInt(7) - 3;
      out.add(new BlockPlacement(px, y, pz, blockType));
    } 
  }
  
  private void generateWatchtowerPlan(List<BlockPlacement> out, int x, int y, int z) {
    int blockType = chooseBlockType(12);
    int height = 6 + random.nextInt(4);
    for (int iy = 0; iy < height; iy++) {
      for (int i = -1; i <= 1; i++) {
        for (int iz = -1; iz <= 1; iz++) {
          if (iy == 0 || Math.abs(i) == 1 || Math.abs(iz) == 1)
            out.add(new BlockPlacement(x + i, y + iy, z + iz, blockType)); 
        } 
      } 
    } 
    for (int ix = -2; ix <= 2; ix++) {
      for (int iz = -2; iz <= 2; iz++)
        out.add(new BlockPlacement(x + ix, y + height, z + iz, 2)); 
    } 
  }
  
  private void generateMarketPlan(List<BlockPlacement> out, int x, int y, int z) {
    int blockType = chooseBlockType(13);
    for (int i = 0; i < 4; i++) {
      int sx = x + i % 2 * 4 - 2;
      int sz = z + i / 2 * 4 - 2;
      int ix;
      for (ix = 0; ix < 2; ix++) {
        for (int iz = 0; iz < 2; iz++)
          out.add(new BlockPlacement(sx + ix, y, sz + iz, 1)); 
      } 
      for (ix = -1; ix < 3; ix++) {
        for (int iz = -1; iz < 3; iz++)
          out.add(new BlockPlacement(sx + ix, y + 2, sz + iz, 2)); 
      } 
    } 
  }
  
  private void generateFountainPlan(List<BlockPlacement> out, int x, int y, int z) {
    int blockType = chooseBlockType(14);
    for (int r = 3; r >= 0; r--) {
      for (int ix = -r; ix <= r; ix++) {
        for (int iz = -r; iz <= r; iz++) {
          if (Math.abs(ix) == r || Math.abs(iz) == r)
            out.add(new BlockPlacement(x + ix, y, z + iz, blockType)); 
        } 
      } 
    } 
    for (int iy = 1; iy <= 3; iy++)
      out.add(new BlockPlacement(x, y + iy, z, 1)); 
  }
  
  public void handlePlayerInteraction(String action) {
    this.lastPlayerAction = action;
    this.lastInteractionTime = System.currentTimeMillis();
    if (action.equals("praise")) {
      this.experience += 10;
      this.currentEmotion = Emotion.EXCITED;
      System.out.println("Builder is excited! Experience: " + this.experience);
    } else if (action.equals("follow")) {
      this.currentEmotion = Emotion.HAPPY;
    } else if (action.equals("stop")) {
      this.hasTarget = false;
      this.currentEmotion = Emotion.CONFUSED;
    } 
    checkLevelUp();
  }
  
  private void checkLevelUp() {
    int requiredExp = this.builderLevel * 100;
    if (this.experience >= requiredExp) {
      this.builderLevel++;
      System.out.println("LEVEL UP! Builder is now level " + this.builderLevel + "!");
      this.currentEmotion = Emotion.EXCITED;
      unlockNewStructures();
    } 
  }
  
  private void unlockNewStructures() {
    System.out.println("Unlocked new building types at level " + this.builderLevel);
  }
  
  private void selectNewBuildingTask() {
    if (this.builderLevel >= 3 && TASK_CHAINS.length > 0) {
      useTaskChain();
    } else {
      this.structureType = random.nextInt(4);
    } 
    this.buildingProgress = 0;
    double distance = 10.0D + random.nextDouble() * 20.0D;
    double angle = random.nextDouble() * Math.PI * 2.0D;
    this.targetX = this.x + Math.cos(angle) * distance;
    this.targetZ = this.z + Math.sin(angle) * distance;
    this.targetY = ((getLevel()).depth - 21);
    adaptToTerrain();
    System.out.println("New building task: " + getStructureName() + " at (" + (int)this.targetX + "," + (int)this.targetY + "," + (int)this.targetZ + ") entity height: " + this.y + " level depth: " + (getLevel()).depth + " Emotion: " + this.currentEmotion);
    this.hasTarget = true;
  }
  
  private void useTaskChain() {
    if (this.currentTaskChain >= TASK_CHAINS.length)
      this.currentTaskChain = 0; 
    int[] currentChain = TASK_CHAINS[this.currentTaskChain];
    if (this.currentTaskIndex >= currentChain.length) {
      this.currentTaskIndex = 0;
      this.currentTaskChain++;
      if (this.currentTaskChain >= TASK_CHAINS.length)
        this.currentTaskChain = 0; 
    } 
    this.structureType = currentChain[this.currentTaskIndex];
    this.currentTaskIndex++;
    System.out.println("Following task chain: " + getStructureName() + " (Chain " + (this.currentTaskChain + 1) + "/" + currentChain.length + ")");
  }
  
  private void adaptToTerrain() {
    int groundY = findGroundLevel((int)this.targetX, (int)this.targetZ);
    if (groundY > 0)
      this.targetY = (groundY + 1); 
  }
  
  private int findGroundLevel(int x, int z) {
    for (int y = (getLevel()).depth - 1; y >= 0; y--) {
      if (getLevel().isTile(x, y, z))
        return y; 
    } 
    return (getLevel()).depth - 10;
  }
  
  private void executeBuildingTask() {
    double distToTarget = Math.sqrt(
        Math.pow(this.x - this.targetX, 2.0D) + 
        Math.pow(this.z - this.targetZ, 2.0D));
    if (distToTarget < 2.0D) {
      System.out.println("Reached target, distance: " + distToTarget);
      buildStructure();
      this.buildingProgress++;
      if (this.buildingProgress >= getMaxBuildingProgress()) {
        this.hasTarget = false;
        System.out.println("Structure completed!");
      } 
    } 
  }
  
  private void buildStructure() {
    System.out.println("Building " + getStructureName() + " at (" + (int)this.targetX + "," + (int)this.targetY + "," + (int)this.targetZ + ") progress: " + this.buildingProgress + " Level: " + this.builderLevel + " Emotion: " + this.currentEmotion);
    switch (this.structureType) {
      case 0:
        buildHouse();
        break;
      case 1:
        buildTower();
        break;
      case 2:
        buildBridge();
        break;
      case 3:
        buildWall();
        break;
      case 4:
        buildCastle();
        break;
      case 5:
        buildVillage();
        break;
      case 6:
        buildFarm();
        break;
      case 7:
        buildTemple();
        break;
      case 8:
        buildComplexBridge();
        break;
      case 9:
        buildRoad();
        break;
    } 
  }
  
  private void buildHouse() {
    int size = 3;
    int height = this.buildingProgress;
    int blockType = chooseBlockType(0);
    for (int x = -size; x <= size; x++) {
      for (int z = -size; z <= size; z++) {
        if (Math.abs(x) == size || Math.abs(z) == size)
          for (int y = 0; y <= height; y++) {
            int blockX = (int)(this.targetX + x);
            int blockY = (int)(this.targetY + y);
            int blockZ = (int)(this.targetZ + z);
            this.level.setTile(blockX, blockY, blockZ, blockType);
            System.out.println("Placed " + ((blockType == 2) ? "wood" : "rock") + " block at (" + blockX + "," + blockY + "," + blockZ + ")");
          }  
      } 
    } 
    forceChunkUpdate();
  }
  
  private void forceChunkUpdate() {
    int radius = 4;
    for (int x = (int)this.targetX - radius; x <= (int)this.targetX + radius; x++) {
      for (int y = (int)this.targetY - radius; y <= (int)this.targetY + radius; y++) {
        for (int z = (int)this.targetZ - radius; z <= (int)this.targetZ + radius; z++) {
          if (this.level.isTile(x, y, z))
            this.level.setTile(x, y, z, 1); 
        } 
      } 
    } 
  }
  
  private void buildTower() {
    int height = this.buildingProgress * 2;
    int blockType = chooseBlockType(1);
    System.out.println("Building tower height: " + height + " with " + ((blockType == 2) ? "wood" : "rock"));
    for (int y = 0; y <= height; y++) {
      int blockX = (int)this.targetX;
      int blockY = (int)(this.targetY + y);
      int blockZ = (int)this.targetZ;
      System.out.println("Placing " + ((blockType == 2) ? "wood" : "rock") + " tower block at (" + blockX + "," + blockY + "," + blockZ + ")");
      this.level.setTile(blockX, blockY, blockZ, blockType);
    } 
    forceChunkUpdate();
  }
  
  private void buildBridge() {
    int length = this.buildingProgress * 2;
    int blockType = chooseBlockType(2);
    for (int i = -length; i <= length; i++) {
      int blockX = (int)(this.targetX + i);
      int blockY = (int)this.targetY;
      int blockZ = (int)this.targetZ;
      this.level.setTile(blockX, blockY, blockZ, blockType);
      System.out.println("Placed " + ((blockType == 2) ? "wood" : "rock") + " bridge block at (" + blockX + "," + blockY + "," + blockZ + ")");
    } 
    forceChunkUpdate();
  }
  
  private void buildWall() {
    int length = this.buildingProgress * 2;
    int blockType = chooseBlockType(3);
    for (int i = -length; i <= length; i++) {
      for (int y = 0; y <= 3; y++) {
        int blockX = (int)(this.targetX + i);
        int blockY = (int)(this.targetY + y);
        int blockZ = (int)this.targetZ;
        this.level.setTile(blockX, blockY, blockZ, blockType);
        System.out.println("Placed " + ((blockType == 2) ? "wood" : "rock") + " wall block at (" + blockX + "," + blockY + "," + blockZ + ")");
      } 
    } 
    forceChunkUpdate();
  }
  
  private void moveToTarget() {
    if (this.useAdvancedPathfinding && random.nextFloat() < 0.4F) {
      List<int[]> path = findAStarPath((int)this.targetX, (int)this.targetZ);
      if (path != null && !path.isEmpty() && 
        moveAlongPath(path))
        return; 
    } 
    double dx = this.targetX - this.x;
    double dz = this.targetZ - this.z;
    double distance = Math.sqrt(dx * dx + dz * dz);
    if (distance > 1.0D) {
      float moveX = (float)(dx / distance);
      float moveZ = (float)(dz / distance);
      moveRelative(moveX, moveZ, 0.02F);
      if (this.onGround && Math.random() < 0.1D)
        this.motionY = 0.15000000596046448D; 
      this.motionY -= 0.004999999888241291D;
      move(this.motionX, this.motionY, this.motionZ);
      this.motionX *= 0.8500000238418579D;
      this.motionY *= 0.9800000190734863D;
      this.motionZ *= 0.8500000238418579D;
    } 
  }
  
  private int getMaxBuildingProgress() {
    switch (this.structureType) {
      case 0:
        return 4;
      case 1:
        return 8;
      case 2:
        return 10;
      case 3:
        return 15;
      case 4:
        return 12;
      case 5:
        return 6;
      case 6:
        return 4;
      case 7:
        return 8;
      case 8:
        return 10;
      case 9:
        return 8;
    } 
    return 5;
  }
  
  private void buildCastle() {
    int size = 6 + this.builderLevel;
    int height = this.buildingProgress * 2;
    int blockType = chooseBlockType(4);
    int x;
    for (x = -size; x <= size; x++) {
      for (int z = -size; z <= size; z++) {
        if (Math.abs(x) == size || Math.abs(z) == size)
          for (int y = 0; y <= height; y++) {
            int blockX = (int)(this.targetX + x);
            int blockY = (int)(this.targetY + y);
            int blockZ = (int)(this.targetZ + z);
            this.level.setTile(blockX, blockY, blockZ, blockType);
            System.out.println("Placed " + ((blockType == 2) ? "wood" : "rock") + " castle block at (" + blockX + "," + blockY + "," + blockZ + ")");
          }  
      } 
    } 
    if (this.buildingProgress >= 2)
      for (x = -size; x <= size; x += size * 2) {
        int z;
        for (z = -size; z <= size; z += size * 2) {
          for (int y = 0; y <= height + 3; y++) {
            int blockX = (int)(this.targetX + x);
            int blockY = (int)(this.targetY + y);
            int blockZ = (int)(this.targetZ + z);
            this.level.setTile(blockX, blockY, blockZ, blockType);
            System.out.println("Placed " + ((blockType == 2) ? "wood" : "rock") + " castle tower block at (" + blockX + "," + blockY + "," + blockZ + ")");
          } 
        } 
      }  
    forceChunkUpdate();
  }
  
  private void buildVillage() {
    int houses = Math.min(3 + this.builderLevel, 8);
    int blockType = chooseBlockType(5);
    for (int i = 0; i < houses; i++) {
      int houseX = (int)(this.targetX + ((i - houses / 2) * 8));
      int houseZ = (int)(this.targetZ + (i % 2 * 8));
      for (int x = -2; x <= 2; x++) {
        for (int z = -2; z <= 2; z++) {
          if (Math.abs(x) == 2 || Math.abs(z) == 2)
            for (int y = 0; y <= 3; y++) {
              this.level.setTile(houseX + x, (int)this.targetY + y, houseZ + z, blockType);
              System.out.println("Placed " + ((blockType == 2) ? "wood" : "rock") + " village house block at (" + (houseX + x) + "," + ((int)this.targetY + y) + "," + (houseZ + z) + ")");
            }  
        } 
      } 
    } 
    forceChunkUpdate();
  }
  
  private void buildFarm() {
    int size = 5 + this.builderLevel;
    int x;
    for (x = -size; x <= size; x++) {
      for (int z = -size; z <= size; z++) {
        if (Math.abs(x) == size || Math.abs(z) == size)
          this.level.setTile((int)this.targetX + x, (int)this.targetY, (int)this.targetZ + z, 1); 
      } 
    } 
    for (x = -1; x <= 1; x++) {
      for (int z = -1; z <= 1; z++) {
        for (int y = 0; y <= 2; y++)
          this.level.setTile((int)this.targetX + x, (int)this.targetY + y, (int)this.targetZ + z, 1); 
      } 
    } 
    forceChunkUpdate();
  }
  
  private void buildTemple() {
    int size = 4 + this.builderLevel;
    int height = this.buildingProgress * 3;
    int x;
    for (x = -size; x <= size; x++) {
      for (int z = -size; z <= size; z++)
        this.level.setTile((int)this.targetX + x, (int)this.targetY, (int)this.targetZ + z, 1); 
    } 
    for (x = -size; x <= size; x += size) {
      int z;
      for (z = -size; z <= size; z += size) {
        for (int y = 0; y <= height; y++)
          this.level.setTile((int)this.targetX + x, (int)this.targetY + y, (int)this.targetZ + z, 1); 
      } 
    } 
    forceChunkUpdate();
  }
  
  private void buildComplexBridge() {
    int length = this.buildingProgress * 4 + this.builderLevel * 2;
    for (int i = -length; i <= length; i++) {
      this.level.setTile((int)(this.targetX + i), (int)this.targetY, (int)this.targetZ, 1);
      if (i % 4 == 0)
        for (int y = -5; y <= 0; y++) {
          this.level.setTile((int)(this.targetX + i), (int)this.targetY + y, (int)this.targetZ, 1);
          this.level.setTile((int)(this.targetX + i), (int)this.targetY + y, (int)this.targetZ - 1, 1);
          this.level.setTile((int)(this.targetX + i), (int)this.targetY + y, (int)this.targetZ + 1, 1);
        }  
    } 
    forceChunkUpdate();
  }
  
  private void buildRoad() {
    int length = this.buildingProgress * 6 + this.builderLevel * 3;
    int width = 2;
    for (int i = -length; i <= length; i++) {
      for (int w = -width; w <= width; w++)
        this.level.setTile((int)(this.targetX + i), (int)this.targetY, (int)(this.targetZ + w), 1); 
    } 
    forceChunkUpdate();
  }
  
  public boolean isBuilding() {
    return (this.isAIBuilder && this.hasTarget);
  }
  
  public String getStructureName() {
    if (!this.isAIBuilder)
      return "Not a Builder"; 
    switch (this.structureType) {
      case 0:
        return "House";
      case 1:
        return "Tower";
      case 2:
        return "Bridge";
      case 3:
        return "Wall";
      case 4:
        return "Castle";
      case 5:
        return "Village";
      case 6:
        return "Farm";
      case 7:
        return "Temple";
      case 8:
        return "Complex Bridge";
      case 9:
        return "Road";
    } 
    return "Unknown";
  }
  
  public String getAIStatus() {
    if (!this.isAIBuilder)
      return "Not an AI Builder"; 
    return String.format("Level %d | Mood %.2f | Creativity %d | Art %d | Experiments %d | %s", new Object[] { Integer.valueOf(this.builderLevel), 
          Float.valueOf(this.moodValue), 
          Integer.valueOf(this.creativityLevel), 
          Integer.valueOf(this.artPiecesCreated), 
          Integer.valueOf(this.experimentCount), this.currentEmotion });
  }
  
  private void updateMapMemory() {
    int currentX = clamp((int)this.x, 0, this.mapWidth - 1);
    int currentZ = clamp((int)this.z, 0, this.mapHeight - 1);
    if (!this.exploredMap[currentX][currentZ]) {
      this.exploredMap[currentX][currentZ] = true;
      System.out.println("AI explored new area: (" + currentX + "," + currentZ + ")");
    } 
    if (this.useAdvancedPathfinding && this.ticksSinceLastBuild - this.lastPathfindingUpdate > 100) {
      updatePathfindingMap();
      this.lastPathfindingUpdate = this.ticksSinceLastBuild;
    } 
  }
  
  private void updatePathfindingMap() {
    int startX = Math.max(0, (int)this.x - 50);
    int endX = Math.min(this.mapWidth - 1, (int)this.x + 50);
    int startZ = Math.max(0, (int)this.z - 50);
    int endZ = Math.min(this.mapHeight - 1, (int)this.z + 50);
    for (int x = startX; x <= endX; x++) {
      for (int z = startZ; z <= endZ; z++) {
        int groundY = findGroundLevel(x, z);
        if (groundY > 0 && groundY < this.mapDepth - 1) {
          this.pathfindingMap[x][z] = 0;
        } else {
          this.pathfindingMap[x][z] = -1;
        } 
      } 
    } 
  }
  
  private List<int[]> findAStarPath(int targetX, int targetZ) {
    if (!this.useAdvancedPathfinding)
      return null; 
    int startX = clamp((int)this.x, 0, this.mapWidth - 1);
    int startZ = clamp((int)this.z, 0, this.mapHeight - 1);
    targetX = clamp(targetX, 0, this.mapWidth - 1);
    targetZ = clamp(targetZ, 0, this.mapHeight - 1);
    List<int[]> path = (List)new ArrayList<>();
    int currentX = startX;
    int currentZ = startZ;
    while (currentX != targetX || currentZ != targetZ) {
      int dx = targetX - currentX;
      int dz = targetZ - currentZ;
      if (Math.abs(dx) > Math.abs(dz)) {
        currentX += (dx > 0) ? 1 : -1;
      } else {
        currentZ += (dz > 0) ? 1 : -1;
      } 
      if (currentX >= 0 && currentX < this.mapWidth && currentZ >= 0 && currentZ < this.mapHeight && this.pathfindingMap[currentX][currentZ] >= 0) {
        path.add(new int[] { currentX, currentZ });
      } else {
        System.out.println("AI: Path blocked, finding alternative route");
        return null;
      } 
      if (path.size() > 100)
        return null; 
    } 
    return path;
  }
  
  private boolean moveAlongPath(List<int[]> path) {
    if (path == null || path.isEmpty())
      return false; 
    int[] nextPoint = path.get(0);
    double dx = nextPoint[0] - this.x;
    double dz = nextPoint[1] - this.z;
    double distance = Math.sqrt(dx * dx + dz * dz);
    if (distance < 1.0D) {
      path.remove(0);
      return true;
    } 
    this.motionX = dx / distance * 0.1D;
    this.motionZ = dz / distance * 0.1D;
    return true;
  }
  
  private void enableMultiStoryBuilding() {
    if (this.builderLevel >= 3 && !this.buildingMultiStory) {
      this.buildingMultiStory = true;
      this.maxFloors = Math.min(2 + this.builderLevel / 2, 5);
      System.out.println("AI enabled multi-story building! Max floors: " + this.maxFloors);
    } 
  }
  
  private void buildNextFloor() {
    if (this.currentFloor < this.maxFloors) {
      this.currentFloor++;
      System.out.println("AI building floor " + this.currentFloor + " of " + this.maxFloors);
      this.targetY += 4.0D;
      this.buildingProgress = 0;
    } 
  }
  
  private void createGarden(int centerX, int centerZ, int size) {
    int groundY = findGroundLevel(centerX, centerZ);
    for (int x = -size; x <= size; x++) {
      for (int z = -size; z <= size; z++) {
        if (Math.abs(x) + Math.abs(z) <= size) {
          int blockX = centerX + x;
          int blockZ = centerZ + z;
          if (random.nextFloat() < 0.3F) {
            int flowerType = random.nextInt(3);
            this.level.setTile(blockX, groundY + 1, blockZ, 2);
          } 
        } 
      } 
    } 
    for (int i = -size; i <= size; i++) {
      this.level.setTile(centerX + i, groundY, centerZ, 1);
      this.level.setTile(centerX, groundY, centerZ + i, 1);
    } 
    this.gardenCount++;
    this.moodValue = Math.min(1.0F, this.moodValue + 0.05F);
    System.out.println("AI created garden #" + this.gardenCount);
    forceChunkUpdate();
  }
  
  private void createFountain(int centerX, int centerZ) {
    int groundY = findGroundLevel(centerX, centerZ);
    for (int r = 3; r >= 0; r--) {
      for (int angle = 0; angle < 360; angle += 30) {
        double rad = Math.toRadians(angle);
        int x = centerX + (int)Math.round(Math.cos(rad) * r);
        int z = centerZ + (int)Math.round(Math.sin(rad) * r);
        this.level.setTile(x, groundY, z, 1);
      } 
    } 
    int y;
    for (y = 0; y <= 3; y++)
      this.level.setTile(centerX, groundY + y, centerZ, 1); 
    for (y = 1; y <= 2; y++)
      this.level.setTile(centerX, groundY + y, centerZ, 2); 
    this.fountainCount++;
    this.moodValue = Math.min(1.0F, this.moodValue + 0.08F);
    System.out.println("AI created fountain #" + this.fountainCount);
    forceChunkUpdate();
  }
  
  private void createRoad(int startX, int startZ, int endX, int endZ) {
    int groundY = findGroundLevel(startX, startZ);
    int dx = Math.abs(endX - startX);
    int dz = Math.abs(endZ - startZ);
    int sx = (startX < endX) ? 1 : -1;
    int sz = (startZ < endZ) ? 1 : -1;
    int err = dx - dz;
    int x = startX;
    int z = startZ;
    while (true) {
      this.level.setTile(x, groundY, z, 1);
      this.level.setTile(x + 1, groundY, z, 1);
      this.level.setTile(x - 1, groundY, z, 1);
      this.level.setTile(x, groundY, z + 1, 1);
      this.level.setTile(x, groundY, z - 1, 1);
      if (x == endX && z == endZ)
        break; 
      int e2 = 2 * err;
      if (e2 > -dz) {
        err -= dz;
        x += sx;
      } 
      if (e2 < dx) {
        err += dx;
        z += sz;
      } 
    } 
    this.roadCount++;
    System.out.println("AI created road #" + this.roadCount);
    forceChunkUpdate();
  }
  
  private void checkAndRepairStructures() {
    if (this.repairAttempts > 10 && this.successfulRepairs < this.repairAttempts / 2) {
      adaptStrategy();
      return;
    } 
    int checkRadius = 10;
    int repairsMade = 0;
    for (int x = -checkRadius; x <= checkRadius; x++) {
      for (int z = -checkRadius; z <= checkRadius; z++) {
        int checkX = clamp((int)this.x + x, 0, this.mapWidth - 1);
        int checkZ = clamp((int)this.z + z, 0, this.mapHeight - 1);
        if (this.structureMemory[checkX][checkZ] > 0) {
          int groundY = findGroundLevel(checkX, checkZ);
          for (int y = groundY; y < groundY + 5; y++) {
            if (!this.level.isTile(checkX, y, checkZ) && this.structureMemory[checkX][checkZ] == 1) {
              int repairBlock = chooseBlockType(0);
              this.level.setTile(checkX, y, checkZ, repairBlock);
              repairsMade++;
            } 
          } 
        } 
      } 
    } 
    if (repairsMade > 0) {
      this.successfulRepairs++;
      this.moodValue = Math.min(1.0F, this.moodValue + 0.03F);
      System.out.println("AI repaired " + repairsMade + " blocks. Success rate: " + (this.successfulRepairs * 100 / 
          Math.max(1, this.repairAttempts)) + "%");
      forceChunkUpdate();
    } 
    this.repairAttempts++;
  }
  
  private void adaptStrategy() {
    this.strategyChanges++;
    System.out.println("AI adapting strategy! Change #" + this.strategyChanges);
    this.repairAttempts = 0;
    this.successfulRepairs = 0;
    if (this.strategyChanges % 2 == 0) {
      this.personalityTraits[0] = random.nextFloat();
      this.personalityTraits[1] = random.nextFloat();
      this.personalityTraits[2] = random.nextFloat();
      System.out.println("AI changed personality traits: creativity=" + 
          String.format("%.2f", new Object[] { Float.valueOf(this.personalityTraits[0]) }) + ", efficiency=" + 
          String.format("%.2f", new Object[] { Float.valueOf(this.personalityTraits[1]) }) + ", aesthetics=" + 
          String.format("%.2f", new Object[] { Float.valueOf(this.personalityTraits[2]) }));
    } 
    this.moodValue = Math.max(0.3F, Math.min(0.8F, this.moodValue + (random.nextFloat() - 0.5F) * 0.2F));
  }
  
  public double getTargetX() {
    return this.targetX;
  }
  
  public double getTargetZ() {
    return this.targetZ;
  }
  
  public boolean getOnGround() {
    return this.onGround;
  }
  
  public int getCreativityLevel() {
    return this.creativityLevel;
  }
  
  public double getMoodValue() {
    return this.moodValue;
  }
  
  public boolean isStuck() {
    return (this.stuckTicks > 40);
  }
  
  public void moveForward() {
    float forward = (float)Math.cos(this.rotation);
    float right = (float)Math.sin(this.rotation);
    moveRelative(forward, right, 0.02F);
  }
  
  public void jump() {
    if (this.onGround)
      this.motionY = 0.3D; 
  }
  
  public void tryPlaceBlock() {
    if (this.placeCooldown == 0) {
      int bx = (int)this.x;
      int by = (int)this.y;
      int bz = (int)this.z;
      if (!this.level.isTile(bx, by, bz)) {
        this.level.setTile(bx, by, bz, 2);
        this.placeCooldown = 3;
      } 
    } 
  }
  
  public void turnLeft() {
    this.rotation -= 0.10000000149011612D;
  }
  
  public void turnRight() {
    this.rotation += 0.10000000149011612D;
  }
  
  public void setAIManager(AIManager manager) {
    this.aiManager = manager;
  }
  
  public AIManager getAIManager() {
    return this.aiManager;
  }
}
