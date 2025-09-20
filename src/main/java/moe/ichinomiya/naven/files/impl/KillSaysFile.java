package moe.ichinomiya.naven.files.impl;

import moe.ichinomiya.naven.Naven;
import moe.ichinomiya.naven.files.ClientFile;
import moe.ichinomiya.naven.modules.impl.misc.KillSay;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class KillSaysFile extends ClientFile {
    private static final String[] styles = {"%s L",
            "%s fw",
            "%s 我喜欢你",
            "%s 我喜欢你♥",
            "%s 兄弟你好香",
            "%s 可以和我交往吗？",
            "%s 你好可爱",
            "%s 别急",
            "%s 你已被清朝杀手陈安健害死！快使用Hack Lunar！",
            "%s 你已被狂笑的蛇陈安健本人害死，快使用我编写的Hack Lunar端！"
    };

    public KillSaysFile() {
        super("killsays.cfg");
    }

    @Override
    public void read(BufferedReader reader) throws IOException {
        KillSay module = (KillSay) Naven.getInstance().getModuleManager().getModule(KillSay.class);
        List<BooleanValue> values = module.getValues();

        String line;
        while ((line = reader.readLine()) != null) {
            values.add(ValueBuilder.create(module, line).setDefaultBooleanValue(false).build().getBooleanValue());
        }

        if (values.isEmpty()) {
            for (String style : styles) {
                values.add(ValueBuilder.create(module, style).setDefaultBooleanValue(false).build().getBooleanValue());
            }
        }
    }

    @Override
    public void save(BufferedWriter writer) throws IOException {
        KillSay module = (KillSay) Naven.getInstance().getModuleManager().getModule(KillSay.class);
        List<BooleanValue> values = module.getValues();

        for (BooleanValue value : values) {
            writer.write(value.getName() + "\n");
        }
    }
}
