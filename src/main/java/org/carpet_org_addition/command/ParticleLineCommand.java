package org.carpet_org_addition.command;

import carpet.script.utils.ParticleParser;
import carpet.utils.CommandHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.carpet_org_addition.CarpetOrgAdditionSettings;
import org.carpet_org_addition.util.CommandUtils;
import org.carpet_org_addition.util.MessageUtils;
import org.carpet_org_addition.util.task.DrawParticleLineTask;
import org.carpet_org_addition.util.task.ServerTaskManagerInterface;

public class ParticleLineCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("particleLine")
                .requires(source -> CommandHelper.canUseCommand(source, CarpetOrgAdditionSettings.commandParticleLine))
                .then(CommandManager.argument("from", Vec3ArgumentType.vec3())
                        .then(CommandManager.argument("to", Vec3ArgumentType.vec3())
                                .executes(ParticleLineCommand::draw))));
    }

    // 准备绘制粒子线
    public static int draw(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        // 获取玩家对象
        ServerPlayerEntity player = CommandUtils.getSourcePlayer(context);
        // 获取粒子线的起始和结束点
        Vec3d from = Vec3ArgumentType.getVec3(context, "from");
        Vec3d to = Vec3ArgumentType.getVec3(context, "to");
        // 获取粒子的效果类型
        ParticleEffect mainParticle = ParticleParser.getEffect("dust 0 0 0 1",
                player.getWorld().createCommandRegistryWrapper(RegistryKeys.PARTICLE_TYPE));
        // 计算粒子线的长度（平方）
        double distanceTo = from.squaredDistanceTo(to);
        // 计算粒子线长度
        int distance = (int) Math.round(Math.sqrt(distanceTo));
        if (distance == 0) {
            return 0;
        }
        ServerTaskManagerInterface worldInterface = ServerTaskManagerInterface.getInstance(player.getServer());
        // 新建绘制粒子线任务
        worldInterface.addTask(new DrawParticleLineTask(player.getServerWorld(), mainParticle, from, to));
        // 发送箭头
        sendArrow(player, to);
        // 返回值为粒子线的长度
        return distance;
    }

    /**
     * 发送箭头文本用来指示方向
     *
     * @see net.minecraft.client.gui.hud.SubtitlesHud#render(DrawContext)
     */
    private static void sendArrow(ServerPlayerEntity player, Vec3d to) {
        // 获取玩家眼睛的位置
        Vec3d eyePos = player.getEyePos();
        Vec3d vec3d2 = new Vec3d(0.0, 0.0, -1.0).rotateX(-player.getPitch() * ((float) Math.PI / 180)).rotateY(-player.getYaw() * ((float) Math.PI / 180));
        Vec3d vec3d3 = new Vec3d(0.0, 1.0, 0.0).rotateX(-player.getPitch() * ((float) Math.PI / 180)).rotateY(-player.getYaw() * ((float) Math.PI / 180));
        Vec3d vec3d4 = vec3d2.crossProduct(vec3d3);
        Vec3d vec3d5 = to.subtract(eyePos).normalize();
        // 视线与垂直方向的夹角
        double verticalAngle = -vec3d4.dotProduct(vec3d5);
        double f = -vec3d2.dotProduct(vec3d5);
        if (f <= 0.5) {
            if (verticalAngle > 0.0) {
                MessageUtils.sendTextMessageToHud(player, Text.literal("-->"));
            } else if (verticalAngle < 0.0) {
                MessageUtils.sendTextMessageToHud(player, Text.literal("<--"));
            }
        }
    }
}
