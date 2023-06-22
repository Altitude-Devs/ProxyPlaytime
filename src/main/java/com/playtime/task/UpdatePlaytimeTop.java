package com.playtime.task;

import com.playtime.config.Config;
import com.playtime.database.Queries;
import com.playtime.util.objects.PlaytimeTop;
import com.playtime.util.objects.TopType;
import org.slf4j.Logger;

//TODO create a task that moves old sessions to the old session table and just replaces them with one big session containing all their pt older than 6 months
public class UpdatePlaytimeTop implements Runnable {

    private final PlaytimeTop playtimeTop;

    public UpdatePlaytimeTop(PlaytimeTop playtimeTop) {
        this.playtimeTop = playtimeTop;
    }

    @Override
    public void run() {
        for (TopType topType : TopType.values()) {
            playtimeTop.prepUpdateTop(topType);
            Queries.updateTopPlayers(Config.TOP_SIZE, topType, playtimeTop);
            playtimeTop.prepUpdateTop(topType);
        }
    }
}
