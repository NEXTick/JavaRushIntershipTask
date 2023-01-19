package com.game.service;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.exception.BadRequestException;
import com.game.exception.NotFoundException;
import com.game.repository.PlayersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;

@Service
public class PlayersService {
    private final PlayersRepository playersRepository;

    private final Logger logger = Logger.getLogger(PlayersService.class.getName());

    @Autowired
    public PlayersService(PlayersRepository playersRepository) {
        this.playersRepository = playersRepository;
    }

    public List<Player> findAll(Specification<Player> specification, Pageable paging) {
        logger.info("FINDING BY FILTERS...");
        Page<Player> pageResult = playersRepository.findAll(specification, paging);
            return pageResult.getContent();
    }

    public ResponseEntity<Player> findOne(long id) {
        Optional<Player> foundPlayer = playersRepository.findById(id);
        if (foundPlayer.isPresent()) {
            Player player = foundPlayer.get();
            logger.info("GETTING player by ID...");
            return new ResponseEntity<>(player,HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    public Player save(Player player) {

        if (isPossibleToSave(player)) {
            logger.info("SAVING player...");
            if (player.getBanned() == null) player.setBanned(false);
            player.setLAUNL();
            return playersRepository.save(player);
        }
            throw new BadRequestException("Please check parameters");
    }

    public Player update(long id, Player player) {
        if (isPossibleToUpdate(player)) {
            Optional<Player> foundedPlayer = playersRepository.findById(id);
            if (foundedPlayer.isPresent()) {
                logger.info("UPDATING player...");
                Player playerForUpdate = foundedPlayer.get();
                prepareForUpdate(playerForUpdate, player);
                return playersRepository.save(playerForUpdate);
            }
            throw new NotFoundException("Player is not found");
        }
        throw new BadRequestException("Please check parameters");
    }

    public void delete(long id) {
        logger.info("DELETING player...");
        if (!playersRepository.existsById(id))
            throw new NotFoundException("Player is not found");
        playersRepository.deleteById(id);
    }

    public Integer count(Specification<Player> specification){
        logger.info("PLAYERS COUNT...");
        return playersRepository.findAll(specification).size();
    }

//---------------------------------------------------------------------------------------------

    private boolean isPossibleToUpdate(Player player) {

        boolean possible = true;

        String name = player.getName();
        if (name != null) {
            if (!isValidName(name)) return false;
        }

        String title = player.getTitle();
        if (title != null) {
            if (!isValidTitle(title)) return false;
        }

        Integer experience = player.getExperience();
        if (experience != null) {
            if (!isValidExperience(experience)) return false;
        }

        Date birthday = player.getBirthday();
        if (player.getBirthday() != null) {
            if (!isValidBirthday(birthday)) return false;
        }

        return possible;
    }

    private void prepareForUpdate(Player playerForUpdate, Player requestPlayer) {
        String name = requestPlayer.getName();
        if (name != null) {
            playerForUpdate.setName(name);
        }

        String title = requestPlayer.getTitle();
        if (title != null) {
            playerForUpdate.setTitle(title);
        }

        Integer experience = requestPlayer.getExperience();
        if (experience != null) {
            playerForUpdate.setExperience(experience);
            playerForUpdate.setLAUNL();
        }

        if (requestPlayer.getBirthday() != null) {
            playerForUpdate.setBirthday(requestPlayer.getBirthday());
        }
        Race race = requestPlayer.getRace();
        if (race != null) {
            playerForUpdate.setRace(race);
        }

        Profession profession = requestPlayer.getProfession();
        if (profession != null) {
            playerForUpdate.setProfession(profession);
        }

        Boolean banned = requestPlayer.getBanned();
        if (banned != null) {
            playerForUpdate.setBanned(banned);
        }
    }
    private boolean isPossibleToSave(Player player) {
        return  player != null && player.getName() != null && player.getTitle() != null &&
                player.getRace() != null && player.getProfession() != null &&
                player.getExperience() != null && player.getBirthday() != null &&
                isValidName(player.getName()) && isValidTitle(player.getTitle())
                && isValidExperience(player.getExperience()) && isValidBirthday(player.getBirthday());
    }
    private boolean isValidName(String name) {
        return name.length() < 13 && !name.isEmpty();
    }

    private boolean isValidTitle(String title) {
        return title.length() < 31;
    }

    private boolean isValidExperience(Integer experience) {
        return experience >= 0 && experience <= 10_000_000;
    }

    private boolean isValidBirthday(Date birthday) {
        long date = birthday.getTime();
        return   date >= 946674000482L && date <= 32535205199494L;
    }
//-----------------------------------------------------------------------------------------------
    public Specification<Player> filterByName(String name) {
        return ((root, query, criteriaBuilder) ->
                name == null ? null : criteriaBuilder.like(root.get("name"), "%" + name + "%"));
    }

    public Specification<Player> filterByTitle(String title) {
        return ((root, query, criteriaBuilder) ->
                title == null ? null : criteriaBuilder.like(root.get("title"), "%" + title + "%"));
    }

    public Specification<Player> filterByRace(Race race) {
        return (root, query, criteriaBuilder) ->
                race == null ? null : criteriaBuilder.equal(root.get("race"), race);
    }

    public Specification<Player> filterByProfession(Profession profession) {
        return (root, query, criteriaBuilder) ->
                profession == null ? null : criteriaBuilder.equal(root.get("profession"), profession);
    }

    public Specification<Player> filterByDate(Long after, Long before) {
        return (root, query, criteriaBuilder) -> {
            if (after == null && before == null)
                return null;
            else if(after == null)
                return criteriaBuilder.lessThanOrEqualTo(root.get("birthday"), new Date(before));
            else if(before == null)
                return criteriaBuilder.greaterThanOrEqualTo(root.get("birthday"), new Date(after));
            else
                return criteriaBuilder.between(root.get("birthday"), new Date(after), new Date(before));
        };
    }

    public Specification<Player> filterByBanned(Boolean banned) {
        return (root, query, criteriaBuilder) ->
                banned == null ? null : banned ? criteriaBuilder.isTrue(root.get("banned"))
                        : criteriaBuilder.isFalse(root.get("banned"));
    }

    public Specification<Player> filterByExperience(Integer minExperience, Integer maxExperience) {
        return ((root, query, criteriaBuilder) -> {
            if (minExperience == null && maxExperience == null)
                return null;
            else if(minExperience == null)
                return criteriaBuilder.lessThanOrEqualTo(root.get("experience"), maxExperience);
            else if(maxExperience == null)
                return criteriaBuilder.greaterThanOrEqualTo(root.get("experience"), minExperience);
            else
                return criteriaBuilder.between(root.get("experience"), minExperience, maxExperience);
        });
    }

    public Specification<Player> filterByLevel(Integer minLevel, Integer maxLevel) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (minLevel == null && maxLevel == null)
                return null;
            else if (minLevel == null)
                return criteriaBuilder.lessThanOrEqualTo(root.get("level"), maxLevel);
            else if (maxLevel == null)
                return criteriaBuilder.greaterThanOrEqualTo(root.get("level"), minLevel);
            else
                return criteriaBuilder.between(root.get("level"), minLevel, maxLevel);
        };
    }
}
