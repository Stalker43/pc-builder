package com.example.demo;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CpuRepository cpuRepository;
    private final GpuRepository gpuRepository;
    private final MotherboardRepository motherboardRepository;
    private final RamRepository ramRepository;
    private final PsuRepository psuRepository;
    private final PcCaseRepository pcCaseRepository;

    public DataInitializer(CpuRepository cpuRepository, GpuRepository gpuRepository,
                           MotherboardRepository motherboardRepository, RamRepository ramRepository,
                           PsuRepository psuRepository, PcCaseRepository pcCaseRepository) {
        this.cpuRepository = cpuRepository;
        this.gpuRepository = gpuRepository;
        this.motherboardRepository = motherboardRepository;
        this.ramRepository = ramRepository;
        this.psuRepository = psuRepository;
        this.pcCaseRepository = pcCaseRepository;
    }

    @Override
    public void run(String... args) throws Exception {


        // ПРОЦЕССОРЫ

        if (cpuRepository.count() == 0) {
            cpuRepository.save(createCpu("AMD Ryzen 5 5600X", "AM4", 6, 65, 150.0));
            cpuRepository.save(createCpu("AMD Ryzen 7 5800X3D", "AM4", 8, 105, 320.0));
            cpuRepository.save(createCpu("Intel Core i5-12400F", "LGA1700", 6, 65, 145.0));
            cpuRepository.save(createCpu("Intel Core i7-13700K", "LGA1700", 16, 253, 400.0));
            cpuRepository.save(createCpu("Intel Core i9-14900K", "LGA1700", 24, 253, 580.0));
            System.out.println("✅ Завезены процессоры");
        }



        // ВИДЕОКАРТЫ

        if (gpuRepository.count() == 0) {
            // Передаем: Название, Память, Требуемый БП, Цена
            gpuRepository.save(createGpu("NVIDIA RTX 3060", 12, 550, 280.0));
            gpuRepository.save(createGpu("NVIDIA RTX 4070 Ti", 12, 700, 800.0));
            gpuRepository.save(createGpu("NVIDIA RTX 4090", 24, 850, 1600.0));
            gpuRepository.save(createGpu("AMD Radeon RX 6700 XT", 12, 650, 320.0));
            gpuRepository.save(createGpu("AMD Radeon RX 7900 XTX", 24, 800, 950.0));
            System.out.println("✅ Завезены видеокарты");
        }


        // МАТЕРИНСКИЕ ПЛАТЫ

        if (motherboardRepository.count() == 0) {
            motherboardRepository.save(createMb("ASUS ROG Strix B550-F", "AM4", "ATX", "DDR4", 180.0));
            motherboardRepository.save(createMb("GIGABYTE B450M DS3H", "AM4", "Micro-ATX", "DDR4", 75.0));
            motherboardRepository.save(createMb("MSI MAG B660M MORTAR", "LGA1700", "Micro-ATX", "DDR5", 160.0));
            motherboardRepository.save(createMb("ASUS PRIME Z790-P", "LGA1700", "ATX", "DDR5", 220.0));
            System.out.println("✅ Завезены материнские платы");
        }


        // ОПЕРАТИВНАЯ ПАМЯТЬ

        if (ramRepository.count() == 0) {
            ramRepository.save(createRam("Corsair Vengeance LPX 16GB", "DDR4", 16, 45.0));
            ramRepository.save(createRam("Kingston FURY Beast 32GB", "DDR4", 32, 85.0));
            ramRepository.save(createRam("G.Skill Trident Z5 RGB 32GB", "DDR5", 32, 130.0));
            ramRepository.save(createRam("Crucial Pro 64GB", "DDR5", 64, 210.0));
            System.out.println("✅ Завезена оперативная память");
        }


        // БЛОКИ ПИТАНИЯ

        if (psuRepository.count() == 0) {
            psuRepository.save(createPsu("DeepCool PF500", 500, 40.0));
            psuRepository.save(createPsu("Chieftec Core 600W", 600, 55.0));
            psuRepository.save(createPsu("Corsair RM850x", 850, 140.0));
            psuRepository.save(createPsu("be quiet! Dark Power 13", 1000, 250.0));
            System.out.println("✅ Завезены блоки питания");
        }


        // КОРПУСА

        if (pcCaseRepository.count() == 0) {
            pcCaseRepository.save(createCase("Zalman S2", "ATX", 45.0));
            pcCaseRepository.save(createCase("Thermaltake Versa H18", "Micro-ATX", 50.0));
            pcCaseRepository.save(createCase("NZXT H510", "ATX", 90.0));
            pcCaseRepository.save(createCase("Lian Li O11 Dynamic", "ATX", 150.0));
            System.out.println("✅ Завезены корпуса");
        }

        System.out.println("🎉 СКЛАД ПОЛНОСТЬЮ ЗАПОЛНЕН И ГОТОВ К РАБОТЕ!");
    }



    private Cpu createCpu(String name, String socket, int cores, int tdp, double price) {
        Cpu cpu = new Cpu();
        cpu.setName(name); cpu.setSocket(socket); cpu.setCores(cores); cpu.setTdp(tdp); cpu.setPrice(price);
        return cpu;
    }

    private Gpu createGpu(String name, int memory, int powerRequired, double price) {
        Gpu gpu = new Gpu();
        gpu.setName(name);
        gpu.setMemory(memory);
        gpu.setPowerRequired(powerRequired);
        gpu.setPrice(price);
        return gpu;
    }

    private Motherboard createMb(String name, String socket, String formFactor,String ramType, double price) {
        Motherboard mb = new Motherboard();
        mb.setName(name); mb.setSocket(socket); mb.setFormFactor(formFactor); mb.setRamType(ramType); mb.setPrice(price);
        return mb;
    }

    private Ram createRam(String name, String type, int capacity, double price) {
        Ram ram = new Ram();
        ram.setName(name); ram.setType(type); ram.setCapacity(capacity); ram.setPrice(price);
        return ram;
    }

    private Psu createPsu(String name, int wattage, double price) {
        Psu psu = new Psu();
        psu.setName(name); psu.setWattage(wattage); psu.setPrice(price);
        return psu;
    }

    private PcCase createCase(String name, String formFactor, double price) {
        PcCase pcCase = new PcCase();
        pcCase.setName(name); pcCase.setFormFactor(formFactor); pcCase.setPrice(price);
        return pcCase;
    }
}