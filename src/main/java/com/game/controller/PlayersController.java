package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayersService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/rest/players")
public class PlayersController {

    private final PlayersService playersService;

    private final Logger logger = Logger.getLogger(PlayersController.class.getName());


    public PlayersController(PlayersService playersService) {
        this.playersService = playersService;
    }

    @GetMapping()
    public ResponseEntity<List<Player>> getPlayers(@RequestParam(required = false) String name,
                                                      @RequestParam(required = false) String title,
                                                      @RequestParam(required = false) Race race,
                                                      @RequestParam(required = false) Profession profession,
                                                      @RequestParam(required = false) Long after,
                                                      @RequestParam(required = false) Long before,
                                                      @RequestParam(required = false) Boolean banned,
                                                      @RequestParam(required = false) Integer minExperience,
                                                      @RequestParam(required = false) Integer maxExperience,
                                                      @RequestParam(required = false) Integer minLevel,
                                                      @RequestParam(required = false) Integer maxLevel,
                                                      @RequestParam(required = false, defaultValue = "ID") PlayerOrder order,
                                                      @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
                                                      @RequestParam(required = false, defaultValue = "3") Integer pageSize) {

        logger.info("FINDING ALL players...");
        Pageable paging = PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));
        Specification<Player> specification = Specification
                        .where(playersService.filterByName(name))
                        .and(playersService.filterByTitle(title))
                        .and(playersService.filterByRace(race))
                        .and(playersService.filterByProfession(profession))
                        .and(playersService.filterByDate(after, before))
                        .and(playersService.filterByBanned(banned))
                        .and(playersService.filterByExperience(minExperience, maxExperience))
                        .and(playersService.filterByLevel(minLevel, maxLevel));
        return ResponseEntity.ok(playersService.findAll(specification, paging));
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getPlayersCount(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) Race race,
        @RequestParam(required = false) Profession profession,
        @RequestParam(required = false) Long after,
        @RequestParam(required = false) Long before,
        @RequestParam(required = false) Boolean banned,
        @RequestParam(required = false) Integer minExperience,
        @RequestParam(required = false) Integer maxExperience,
        @RequestParam(required = false) Integer minLevel,
        @RequestParam(required = false) Integer maxLevel) {
        logger.info("PLAYERS COUNT...");
            Specification<Player> specification = Specification
                    .where(playersService.filterByName(name))
                    .and(playersService.filterByTitle(title))
                    .and(playersService.filterByRace(race))
                    .and(playersService.filterByProfession(profession))
                    .and(playersService.filterByDate(after, before))
                    .and(playersService.filterByDate(after, before))
                    .and(playersService.filterByBanned(banned))
                    .and(playersService.filterByExperience(minExperience, maxExperience))
                    .and(playersService.filterByLevel(minLevel, maxLevel));

            return ResponseEntity.ok(playersService.count(specification));
    }

    @PostMapping()
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        logger.info("CREATE PLAYER...");
        return  ResponseEntity.ok(playersService.save(player));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable("id") String id) {
        logger.info("Trying to get player by ID...");
        if (id.chars().allMatch(Character :: isDigit)) {
            long playerId = Long.parseLong(id);
            if (playerId > 0) {
                return playersService.findOne(playerId);
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }


    @PostMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable String id, @RequestBody Player player) {
        logger.info("Trying to update player by ID...");
        if (id.chars().allMatch(Character :: isDigit)) {
            long playerId = Long.parseLong(id);
            if (playerId > 0 && playerId < Long.MAX_VALUE) {
                return ResponseEntity.ok(playersService.update(playerId, player));
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Player> deletePlayer(@PathVariable String id) {
        logger.info("Trying to delete player by ID...");
        if (id.chars().allMatch(Character :: isDigit)) {
            long playerId = Long.parseLong(id);
            if (playerId > 0) {
                playersService.delete(Long.parseLong(id));
                return new ResponseEntity<>(HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
