/*     */ package net.minecraft.client.renderer.block.model.multipart;
/*     */ import com.google.common.collect.Sets;
/*     */ import com.google.gson.JsonArray;
/*     */ import com.google.gson.JsonDeserializationContext;
/*     */ import com.google.gson.JsonDeserializer;
/*     */ import com.google.gson.JsonElement;
/*     */ import com.google.gson.JsonParseException;
/*     */ import com.mojang.datafixers.util.Pair;
/*     */ import java.lang.reflect.Type;
/*     */ import java.util.Collection;
/*     */ import java.util.List;
/*     */ import java.util.Objects;
/*     */ import java.util.Set;
/*     */ import java.util.function.Function;
/*     */ import java.util.stream.Collectors;
/*     */ import java.util.stream.Stream;
/*     */ import javax.annotation.Nullable;
/*     */ import net.minecraft.client.renderer.block.model.BlockModelDefinition;
/*     */ import net.minecraft.client.renderer.block.model.MultiVariant;
/*     */ import net.minecraft.client.renderer.texture.TextureAtlasSprite;
/*     */ import net.minecraft.client.resources.model.BakedModel;
/*     */ import net.minecraft.client.resources.model.Material;
/*     */ import net.minecraft.client.resources.model.ModelBakery;
/*     */ import net.minecraft.client.resources.model.ModelState;
/*     */ import net.minecraft.client.resources.model.MultiPartBakedModel;
/*     */ import net.minecraft.client.resources.model.UnbakedModel;
/*     */ import net.minecraft.resources.ResourceLocation;
/*     */ import net.minecraft.world.level.block.Block;
/*     */ import net.minecraft.world.level.block.state.BlockState;
/*     */ import net.minecraft.world.level.block.state.StateDefinition;
/*     */ import net.minecraftforge.api.distmarker.Dist;
/*     */ import net.minecraftforge.api.distmarker.OnlyIn;
/*     */ 
/*     */ @OnlyIn(Dist.CLIENT)
/*     */ public class MultiPart implements UnbakedModel {
/*     */   private final StateDefinition<Block, BlockState> definition;
/*     */   
/*     */   public MultiPart(StateDefinition<Block, BlockState> p_111965_, List<Selector> p_111966_) {
/*  39 */     this.definition = p_111965_;
/*  40 */     this.selectors = p_111966_;
/*     */   }
/*     */   private final List<Selector> selectors;
/*     */   public List<Selector> getSelectors() {
/*  44 */     return this.selectors;
/*     */   }
/*     */   
/*     */   public Set<MultiVariant> getMultiVariants() {
/*  48 */     Set<MultiVariant> $$0 = Sets.newHashSet();
/*     */     
/*  50 */     for (Selector $$1 : this.selectors) {
/*  51 */       $$0.add($$1.getVariant());
/*     */     }
/*     */     
/*  54 */     return $$0;
/*     */   }
/*     */ 
/*     */   
/*     */   public boolean equals(Object p_111984_) {
/*  59 */     if (this == p_111984_) {
/*  60 */       return true;
/*     */     }
/*  62 */     if (p_111984_ instanceof MultiPart) {
/*  63 */       MultiPart $$1 = (MultiPart)p_111984_;
/*  64 */       return (Objects.equals(this.definition, $$1.definition) && Objects.equals(this.selectors, $$1.selectors));
/*     */     } 
/*  66 */     return false;
/*     */   }
/*     */ 
/*     */   
/*     */   public int hashCode() {
/*  71 */     return Objects.hash(new Object[] { this.definition, this.selectors });
/*     */   }
/*     */ 
/*     */   
/*     */   public Collection<ResourceLocation> getDependencies() {
/*  76 */     return (Collection<ResourceLocation>)getSelectors().stream().flatMap(p_111969_ -> p_111969_.getVariant().getDependencies().stream()).collect(Collectors.toSet());
/*     */   }
/*     */ 
/*     */   
/*     */   public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> p_111976_, Set<Pair<String, String>> p_111977_) {
/*  81 */     return (Collection<Material>)getSelectors().stream().flatMap(p_111981_ -> p_111981_.getVariant().getMaterials(p_111979_, p_111980_).stream()).collect(Collectors.toSet());
/*     */   }
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   public BakedModel bake(ModelBakery p_111971_, Function<Material, TextureAtlasSprite> p_111972_, ModelState p_111973_, ResourceLocation p_111974_) {
/*  87 */     MultiPartBakedModel.Builder $$4 = new MultiPartBakedModel.Builder();
/*     */     
/*  89 */     for (Selector $$5 : getSelectors()) {
/*  90 */       BakedModel $$6 = $$5.getVariant().bake(p_111971_, p_111972_, p_111973_, p_111974_);
/*  91 */       if ($$6 != null) {
/*  92 */         $$4.add($$5.getPredicate(this.definition), $$6);
/*     */       }
/*     */     } 
/*     */     
/*  96 */     return $$4.build();
/*     */   }
/*     */   
/*     */   @OnlyIn(Dist.CLIENT)
/*     */   public static class Deserializer implements JsonDeserializer<MultiPart> { private final BlockModelDefinition.Context context;
/*     */     
/*     */     public Deserializer(BlockModelDefinition.Context p_111989_) {
/* 103 */       this.context = p_111989_;
/*     */     }
/*     */ 
/*     */     
/*     */     public MultiPart deserialize(JsonElement p_111994_, Type p_111995_, JsonDeserializationContext p_111996_) throws JsonParseException {
/* 108 */       return new MultiPart(this.context.getDefinition(), getSelectors(p_111996_, p_111994_.getAsJsonArray()));
/*     */     }
/*     */     
/*     */     private List<Selector> getSelectors(JsonDeserializationContext p_111991_, JsonArray p_111992_) {
/* 112 */       List<Selector> $$2 = Lists.newArrayList();
/*     */       
/* 114 */       for (JsonElement $$3 : p_111992_) {
/* 115 */         $$2.add((Selector)p_111991_.deserialize($$3, Selector.class));
/*     */       }
/*     */       
/* 118 */       return $$2;
/*     */     } }
/*     */ 
/*     */ }


/* Location:              C:\Users\MaximilianHoevelmann\Desktop\forge-1.19.2-43.3.13_mapped_official_1.19.2.jar!\net\minecraft\client\renderer\block\model\multipart\MultiPart.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */