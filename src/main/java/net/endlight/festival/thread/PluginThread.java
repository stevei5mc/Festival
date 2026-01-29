package net.endlight.festival.thread;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.utils.Config;
import net.endlight.festival.Festival;
import net.endlight.festival.utils.Utils;

import java.util.Calendar;
import java.util.Date;

public class PluginThread extends Thread {

    private Config config;

    public PluginThread(Config config) {
        this.config = config;
    }

    public static boolean in23h;

    @Override
    public void run() {
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.set(this.config.getInt("Calendar.Year"),
                (this.config.getInt("Calendar.Month") - 1),
                this.config.getInt("Calendar.Day"),
                this.config.getInt("Calendar.Hour"),
                this.config.getInt("Calendar.Minute"),
                this.config.getInt("Calendar.Second"));

        long endTime = calendar.getTimeInMillis();
        long startTime = date.getTime();
        long midTime = (endTime - startTime) / 1000;

        while (midTime > 0) {
            midTime--;

            long h = midTime / 60 / 60 % 60;
            long m = midTime / 60 % 60;
            long s = midTime % 60;

            if (h <= 23) {
                 in23h = true;
                    String tipMessageA = config.getString("Bottom.A")
                            .replace("@hour", Utils.addZero(h))
                            .replace("@minute", Utils.addZero(m))
                            .replace("@second", Utils.addZero(s));
                    String tipMessageB = config.getString("Bottom.B")
                            .replace("@hour", Utils.addZero(h))
                            .replace("@minute", Utils.addZero(m))
                            .replace("@second", Utils.addZero(s));
                    if (h > 0) {
                        Utils.sendTipToAll(tipMessageA);
                    } else if (s > 30) {
                        Utils.sendTipToAll(tipMessageB);
                    } else if (m >= 1) {
                        Utils.sendTipToAll(tipMessageB);
                    }
                    if (h == 0 && m == 0 && s <= 30) {
                        if (config.getBoolean("SimpleMode")){
                            if (s > 20) {
                                Utils.sendTitleToAll(config.getString("Simple_Title.A").replaceAll("@second", String.valueOf(s)),
                                        config.getString("Simple_SubTitle.A"), 0, 40, 0);
                            } else if (s > 10) {
                                Utils.sendTitleToAll(config.getString("Simple_Title.B").replaceAll("@second", String.valueOf(s)),
                                        config.getString("Simple_SubTitle.B"), 0, 40, 0);
                            } else {
                                Utils.sendTitleToAll(config.getString("Simple_Title.C").replaceAll("@second", String.valueOf(s)),
                                        config.getString("Simple_SubTitle.C"), 0, 40, 0);
                            }
                        } else {
                            String tS = String.valueOf(s);
                            Utils.sendTitleToAll(config.getString("Custom_Title." + tS + "s").replaceAll("@second", tS),
                                    config.getString("Custom_SubTitle." + tS + "s"), 0, 40, 0);
                        }
                        for (Player player : Festival.getInstance().getServer().getOnlinePlayers().values()) {
                             Utils.playSound(player, Sound.NOTE_PLING, 0.840896F);
                             Utils.playSound(player, Sound.NOTE_CHIME, 0.840896F);
                    }
                }
                if (h == 0 && m == 0 && s == 0 ) {
                    Festival.getInstance().getServer().getScheduler().scheduleDelayedTask(Festival.getInstance(), () -> {
                        for (Player player : Festival.getInstance().getServer().getOnlinePlayers().values()) {
                            player.sendTitle(config.getString("Final_Title.Title"),
                                    config.getString("Final_Title.SubTitle"), 10, 60, 10);
                            // 发送标题

                            Utils.spawnFirework(player.getPosition());
                            // 放烟花

                            player.sendMessage(this.config.getString("RewardMessage"));
                            // 奖励消息

                            for (String commands : this.config.getStringList("Rewards")) {
                                String[] cmd = commands.split("&");
                                if ((cmd.length > 1) && (cmd[1].equals("con"))){
                                    Server.getInstance().dispatchCommand(new ConsoleCommandSender(), cmd[0].replace("@player", player.getName()));
                                } else {
                                    Server.getInstance().dispatchCommand(player, cmd[0].replace("@player", player.getName()));
                                }
                            }
                            // 执行奖励命令
                        }
                    },this.config.getInt("DelayTime"));

                    Festival.getInstance().getServer().getScheduler().scheduleDelayedTask(Festival.getInstance(), () -> {
                        Server.getInstance().getCommandMap().dispatch(new ConsoleCommandSender(), this.config.getString("PlayMusicCmd"));
                        // 执行音乐播放命令
                    },this.config.getInt("PlayMusicDelayTime"));

                    Festival.getInstance().getServer().getScheduler().scheduleDelayedRepeatingTask(Festival.getInstance(), () -> {
                        Utils.sendTipToAll(this.config.getString("TipMessage"));
                        // 循环发送底部消息
                    },this.config.getInt("TipMessageDelayTime"),this.config.getInt("TipMessagePeriod"));

                    Festival.getInstance().getServer().getScheduler().scheduleDelayedTask(Festival.getInstance(), () -> {
                        Festival.getInstance().getServer().getScheduler().cancelAllTasks();
                        // 一定时长后取消Task
                    },config.getInt("TipShowTime"));
                    Festival.getInstance().getServer().getScheduler().scheduleDelayedTask(Festival.getInstance(), () -> {
                        in23h = false;
                        // 一定时长后停止燃放烟花
                    },config.getInt("FireworkShowTime"));
                }
            } else {
                in23h = false;
            }
                try {
                    Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                }
        }
    }
}
