package de.farm.app.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatsService {

    @PersistenceContext
    private EntityManager em;

    // Daily sales (sum, orders) between from..to
    public List<Map<String, Object>> salesBuckets(Instant from, Instant to, String interval) {
        String dateExpr = switch (interval) {
            case "monthly" ->
                "date_trunc('month', o.created_at)";
            case "weekly" ->
                "date_trunc('week', o.created_at)";
            default ->
                "date_trunc('day', o.created_at)";
        };
        String sql = """
                    select %s as bucket, sum(o.total_cents) as revenue_cents, count(*) as orders
                    from orders o
                    where o.status = 'PAID' and o.created_at between :from and :to
                    group by bucket
                    order by bucket
                    """.formatted(dateExpr);

        var query = em.createNativeQuery(sql);
        query.setParameter("from", from);
        query.setParameter("to", to);

        return fetchSalesResultList(query);
    }

    public List<Map<String, Object>> topProducts(Instant from, Instant to, int limit) {
        String sql = """
                    select oi.product_id, oi.product_name, sum(oi.quantity) as units, sum(oi.subtotal_cents) as revenue
                    from order_items oi
                    join orders o on oi.order_id = o.id
                    where o.status='PAID' and o.created_at between :from and :to
                    group by oi.product_id, oi.product_name
                    order by revenue desc
                    limit :limit
                    """;
        var query = em.createNativeQuery(sql);
        query.setParameter("from", from);
        query.setParameter("to", to);
        query.setParameter("limit", limit);

        return fetchTopProductsResultList(query);

    }

    // AOV and turnover
    public Map<String,Object> kpis(Instant from, Instant to) {
        String sql = """
                    select count(*) orders, coalesce(sum(total_cents),0) revenue
                    from orders o where o.status='PAID' and o.created_at between :from and :to
                    """;
        var query = em.createNativeQuery(sql);
        query.setParameter("from", from);
        query.setParameter("to", to);
        Object[] row = (Object[]) query.getSingleResult();
        long orders = ((Number)row[0]).longValue();
        long revenue = ((Number)row[1]).longValue();
        double aov = orders == 0 ? 0 : revenue / 100.0 / orders;

        // Simple turnover proxy: units sold / average on-hand (needs inventory snapshots; here sell-through)
        String sqlUnits = """
                        select coalesce(sum(oi.quantity),0) from order_items oi
                        join orders o on oi.order_id=o.id
                        where o.status='PAID' and o.created_at between :from and :to
                        """;
        var units = ((Number) em.createNativeQuery(sqlUnits)
                    .setParameter("from",from)
                    .setParameter("to",to)
                    .getSingleResult())
                    .longValue();
                    
        return Map.of("orders", orders, "revenueCents", revenue, "aov", aov, "unitsSold", units);
    }

    private List<Map<String, Object>> fetchSalesResultList(Query query) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> map = new HashMap<>();
            map.put("bucket", ((java.sql.Timestamp) row[0]).toInstant().toString());
            map.put("revenueCents", ((Number) row[1]).longValue());
            map.put("orders", ((Number) row[2]).intValue());
            result.add(map);
        }
        return result;
    }

    private List<Map<String, Object>> fetchTopProductsResultList(Query query) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> map = new HashMap<>();
            map.put("productId", row[0].toString());
            map.put("name", (String) row[1]);
            map.put("units", ((Number) row[2]).longValue());
            map.put("revenueCents", ((Number) row[3]).longValue());
            result.add(map);
        }

        return result;

    }
}
