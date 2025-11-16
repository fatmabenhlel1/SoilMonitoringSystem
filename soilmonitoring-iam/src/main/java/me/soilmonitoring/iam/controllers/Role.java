package me.soilmonitoring.iam.controllers;

import org.eclipse.microprofile.config.ConfigProvider;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Role {
    GUEST(0L),
    R_P00(1L),R_P01(1L<<1L),R_P02(1L<<2L),R_P03(1L<<3L),R_P04(1L<<4L),R_P05(1L<<5L),
    R_P06(1L<<6L),R_P07(1L<<7L),R_P08(1L<<8L),R_P09(1L<<9L),R_P10(1L<<10L),
    R_P11(1L<<11L),R_P12(1L<<12L),R_P13(1L<<13L),R_P14(1L<<14L),R_P15(1L<<15L),
    R_P16(1L<<16L),R_P17(1L<<17L),R_P18(1L<<18L),R_P19(1L<<19L),R_P20(1L<<20L),
    R_P21(1L<<21L),R_P22(1L<<22L),R_P23(1L<<23L),R_P24(1L<<24L),R_P25(1L<<25L),
    R_P26(1L<<26L),R_P27(1L<<27L),R_P28(1L<<28L),R_P29(1L<<29L),R_P30(1L<<30L),
    R_P31(1L<<31L),R_P32(1L<<32L),R_P33(1L<<33L),R_P34(1L<<34L),R_P35(1L<<35L),
    R_P36(1L<<36L),R_P37(1L<<37L),R_P38(1L<<38L),R_P39(1L<<39L),R_P40(1L<<40L),
    R_P41(1L<<41L),R_P42(1L<<42L),R_P43(1L<<43L),R_P44(1L<<44L),R_P45(1L<<45L),
    R_P46(1L<<46L),R_P47(1L<<47L),R_P48(1L<<48L),R_P49(1L<<49L),R_P50(1L<<50L),
    R_P51(1L<<51L),R_P52(1L<<52L),R_P53(1L<<53L),R_P54(1L<<54L),R_P55(1L<<55L),
    R_P56(1L<<56L),R_P57(1L<<57L),R_P58(1L<<58L),R_P59(1L<<59L),R_P60(1L<<60L),
    R_P61(1L<<61L),R_P62(1L<<62L), ROOT(Long.MAX_VALUE);

    private final long value;

    Role(long value){
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    private static final Map<Long,String> ids = new LinkedHashMap<>();
    private static final Map<String,Role> byIds = new LinkedHashMap<>();

    static {
        final var id = new AtomicLong(1L);
        var customRoles = ConfigProvider.getConfig().getValues("roles",String.class);
        if(customRoles.stream().anyMatch(r -> r.equalsIgnoreCase(GUEST.name())||r.equalsIgnoreCase(ROOT.name()))
                ||customRoles.size()>62){
            throw new IllegalArgumentException("Illegal config value for roles");
        }
        ids.putAll(customRoles.stream().collect(Collectors.toMap(x -> id.getAndUpdate(y -> 2L*y),Function.identity())));
        ids.put(GUEST.value, GUEST.name().toLowerCase());
        ids.put(ROOT.value, ROOT.name().toLowerCase());
        final AtomicInteger ordinal = new AtomicInteger(1);
        final Role[] values = Role.values();
        byIds.put(GUEST.name().toLowerCase(),GUEST);
        byIds.put(ROOT.name().toLowerCase(),ROOT);
        byIds.putAll(customRoles.stream().collect(Collectors.toMap(Function.identity(),x -> values[ordinal.getAndIncrement()])));
    }

    public final String id(){
        return ids.get(value);
    }

    public static String byValue(Long value){
        return ids.get(value);
    }

    public static Role byId(String id){
        return byIds.get(id);
    }
}
