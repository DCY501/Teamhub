package com.teamhub.teamhub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teamhub.teamhub.dto.MatchCreateRequest;
import com.teamhub.teamhub.entity.MatchGame;
import com.teamhub.teamhub.entity.MatchSet;
import com.teamhub.teamhub.mapper.MatchGameMapper;
import com.teamhub.teamhub.mapper.MatchSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MatchService {

    @Autowired
    private MatchGameMapper matchGameMapper;

    @Autowired
    private MatchSetMapper matchSetMapper;

    public List<MatchGame> list(String gameType) {
        QueryWrapper<MatchGame> qw = new QueryWrapper<MatchGame>().orderByDesc("game_time");
        if (gameType != null && !gameType.isEmpty()) {
            qw.eq("game_type", gameType);
        }
        return matchGameMapper.selectList(qw);
    }

    public Map<String, Object> getDetail(Long id) {
        MatchGame game = matchGameMapper.selectById(id);
        List<MatchSet> sets = matchSetMapper.selectList(
            new QueryWrapper<MatchSet>().eq("match_id", id).orderByAsc("set_number")
        );
        Map<String, Object> result = new HashMap<>();
        result.put("game", game);
        result.put("sets", sets);
        return result;
    }

    public void create(MatchCreateRequest req) {
        MatchGame game = new MatchGame();
        game.setOpponent(req.getOpponent());
        game.setGameTime(req.getGameTime());
        game.setLocation(req.getLocation());
        game.setGameType(req.getGameType());
        game.setLineup(req.getLineup());
        game.setNotes(req.getNotes());
        game.setVideoUrl(req.getVideoUrl());
        game.setCreatedBy(req.getCreatedBy());
        game.setStatus("UPCOMING");
        matchGameMapper.insert(game);

        if (req.getSets() != null) {
            for (int i = 0; i < req.getSets().size(); i++) {
                MatchSet s = req.getSets().get(i);
                s.setMatchId(game.getId());
                s.setSetNumber(i + 1);
                matchSetMapper.insert(s);
            }
        }
    }

    @Autowired
    private TimelineService timelineService;

    public void updateStatus(Long id, String status) {
        MatchGame game = matchGameMapper.selectById(id);
        if ("FINISHED".equals(game.getStatus())) return; // 已结束不可改状态
        game.setStatus(status);
        matchGameMapper.updateById(game);
        if ("FINISHED".equals(status)) {
            timelineService.autoMatchFinished(game.getId(), game.getOpponent(), game.getGameType(), game.getCreatedBy());
        }
    }

    public void updateNotes(Long id, String notes) {
        MatchGame game = matchGameMapper.selectById(id);
        if (game == null) throw new RuntimeException("比赛不存在");
        game.setNotes(notes);
        matchGameMapper.updateById(game);
    }

    public void updateSets(Long matchId, List<MatchSet> sets) {
        MatchGame game = matchGameMapper.selectById(matchId);
        if (game == null) throw new RuntimeException("比赛不存在");
        matchSetMapper.delete(new QueryWrapper<MatchSet>().eq("match_id", matchId));
        if (sets != null) {
            for (int i = 0; i < sets.size(); i++) {
                MatchSet s = sets.get(i);
                s.setId(null);
                s.setMatchId(matchId);
                s.setSetNumber(i + 1);
                matchSetMapper.insert(s);
            }
        }
    }

    public void updateLineup(Long id, String lineup) {
        MatchGame game = matchGameMapper.selectById(id);
        if (game == null) throw new RuntimeException("比赛不存在");
        game.setLineup(lineup);
        matchGameMapper.updateById(game);
    }

    public void updateVideoUrl(Long id, String videoUrl) {
        MatchGame game = matchGameMapper.selectById(id);
        if (game == null) throw new RuntimeException("比赛不存在");
        game.setVideoUrl(videoUrl);
        matchGameMapper.updateById(game);
    }
}
