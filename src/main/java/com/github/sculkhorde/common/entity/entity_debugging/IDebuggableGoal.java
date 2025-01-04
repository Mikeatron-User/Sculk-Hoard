package com.github.sculkhorde.common.entity.entity_debugging;

import java.util.Optional;

public interface IDebuggableGoal {

    Optional<String> getLastReasonForGoalNoStart();

    Optional<String> getGoalName();

    long getLastTimeOfGoalExecution();

    long getTimeRemainingBeforeCooldownOver();
}
