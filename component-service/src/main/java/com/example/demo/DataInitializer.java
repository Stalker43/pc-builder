package com.example.demo; // Убедись, что пакет совпадает с твоим

import com.example.demo.entity.Cpu;
import com.example.demo.repository.CpuRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CpuRepository cpuRepository;

    public DataInitializer(CpuRepository cpuRepository) {
        this.cpuRepository = cpuRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Проверяем: если склад пустой, то завозим товар
        if (cpuRepository.count() == 0) {
            Cpu cpu1 = new Cpu();
            cpu1.setName("AMD Ryzen 5 5600X");
            cpu1.setSocket("AM4");
            cpu1.setCores(6);
            cpu1.setTdp(65);
            cpu1.setPrice(150.0);

            Cpu cpu2 = new Cpu();
            cpu2.setName("Intel Core i5-12400F");
            cpu2.setSocket("LGA1700");
            cpu2.setCores(6);
            cpu2.setTdp(65);
            cpu2.setPrice(160.0);

            Cpu cpu3 = new Cpu();
            cpu3.setName("Intel Core i9-13900K");
            cpu3.setSocket("LGA1700");
            cpu3.setCores(24);
            cpu3.setTdp(253);
            cpu3.setPrice(550.0);

            // Сохраняем в базу данных
            cpuRepository.save(cpu1);
            cpuRepository.save(cpu2);
            cpuRepository.save(cpu3);

            System.out.println("✅ Товар успешно завезен: добавлено 3 процессора!");
        }
    }
}