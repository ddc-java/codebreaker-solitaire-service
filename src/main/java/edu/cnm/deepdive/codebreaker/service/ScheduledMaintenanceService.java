/*
 *  Copyright 2022 CNM Ingenuity, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package edu.cnm.deepdive.codebreaker.service;

import edu.cnm.deepdive.codebreaker.model.dao.GameRepository;
import java.util.Calendar;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Provides scheduled database maintenance tasks. Currently, only one such task is
 * implemented&mdash;namely, the deletion of inactive games.
 */
@Service
@Profile("service")
public class ScheduledMaintenanceService {

  private final GameRepository repository;

  @Value("${schedule.stale-game-days}")
  private int staleGameDays;

  /**
   * Initializes the service with a {@link GameRepository}.
   *
   * @param repository Persistence operations provider.
   */
  @Autowired
  public ScheduledMaintenanceService(GameRepository repository) {
    this.repository = repository;
  }

  /**
   * Deletes inactive games&mdash;that is, those games without any guesses submitted in the most
   * recent <i>N</i> days, where <i>N</i> is set from the {@code schedule.stale-game-days}
   * application property.
   */
  @Scheduled(cron = "${schedule.cron}", zone = "${schedule.zone}")
  public void cleanStaleGames() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, -staleGameDays);
    repository.deleteAll(repository.findAllStale(calendar.getTime()));
  }

}
