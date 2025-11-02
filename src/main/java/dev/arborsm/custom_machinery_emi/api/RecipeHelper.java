package dev.arborsm.custom_machinery_emi.api;

import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IComponentGuiElement;
import fr.frinn.custommachinery.common.component.DummyComponentManager;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import net.minecraft.core.BlockPos;

import java.util.Optional;

public class RecipeHelper {

    private final CustomMachine machine;
    private final DummyComponentManager dummyManager;

    public RecipeHelper(CustomMachine machine) {
        this.machine = machine;
        CustomMachineTile tile = new CustomMachineTile(BlockPos.ZERO, Registration.CUSTOM_MACHINE_BLOCK.get().defaultBlockState());
        this.dummyManager = new DummyComponentManager(tile);
        tile.setId(machine.getId());
    }

    public Optional<IMachineComponentTemplate<?>> getComponentForElement(IComponentGuiElement<?> element) {
        return this.machine.getComponentTemplates().stream().filter(template -> {
            if(!template.getId().equals(element.getComponentId()))
                return false;
            //Special case for slot gui element because several components of different types (default, filter, fluid etc...) can map to it.
            if(element.getComponentType() == Registration.ITEM_MACHINE_COMPONENT.get())
                return template instanceof ItemMachineComponent.Template;
            return template.getType() == element.getComponentType();
        }).findFirst();
    }

    public DummyComponentManager getDummyManager() {
        return dummyManager;
    }
}


